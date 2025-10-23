package io.hpp.noosphere.agent.service.blockchain;

import io.hpp.noosphere.agent.service.ComputationService;
import io.hpp.noosphere.agent.service.blockchain.dto.*;
import io.hpp.noosphere.agent.service.dto.*;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

@Service
public class BlockChainService {

    private static final Logger log = LoggerFactory.getLogger(BlockChainService.class);
    private static final String BLOCKED_TX = "0xblocked";

    private final Web3j web3j;
    private final CoordinatorService coordinator;
    private final WalletService wallet;
    private final ComputationService computationService;

    // State management using thread-safe collections
    private final Map<Long, SubscriptionDTO> subscriptions = new ConcurrentHashMap<>();
    private final Map<DelegatedSubscriptionId, DelegatedSubscriptionData> delegateSubscriptions = new ConcurrentHashMap<>();
    private final Map<SubscriptionRunKey, String> pendingTxs = new ConcurrentHashMap<>();
    private final Map<SubscriptionRunKey, AtomicInteger> txAttempts = new ConcurrentHashMap<>();

    public BlockChainService(Web3j web3j, CoordinatorService coordinator, WalletService wallet, ComputationService computationService) {
        this.web3j = web3j;
        this.coordinator = coordinator;
        this.wallet = wallet;
        this.computationService = computationService;
    }

    /**
     * Tracks incoming on-chain messages.
     */
    @Async
    public CompletableFuture<Void> processIncomingRequest(BaseRequestDTO request) {
        if (request instanceof OnchainRequestDTO onchainRequestDTO) {
            ProcessOnchainRequest(onchainRequestDTO);
            return CompletableFuture.completedFuture(null);
        } else if (request instanceof DelegatedRequestDTO delegatedRequestDTO) {
            return ProcessDelegatedRequest(delegatedRequestDTO);
        } else {
            log.error("Unknown request type to track: {}", request);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown request type"));
        }
    }

    private void ProcessOnchainRequest(OnchainRequestDTO requestDTO) {
        subscriptions.put(requestDTO.getSubscription().getId(), requestDTO.getSubscription());
        log.info("Tracked new subscription! id={}, total={}", requestDTO.getSubscription().getId(), subscriptions.size());
    }

    private CompletableFuture<Void> ProcessDelegatedRequest(DelegatedRequestDTO requestDTO) {
        // 1. Collect message inputs
        SubscriptionDTO subscription = requestDTO.getSubscription();
        SignatureParamsDTO signature = requestDTO.getSignature();

        // Start the async chain by fetching the latest block number asynchronously
        return CompletableFuture.supplyAsync(() -> {
            try {
                return web3j.ethBlockNumber().send().getBlockNumber().longValue();
            } catch (IOException e) {
                log.error("Failed to get latest block number", e);
                throw new RuntimeException(e);
            }
        }).thenCompose(headBlock ->
            // 2. Check if delegated subscription already exists on-chain
            coordinator
                .getExistingDelegateSubscription(subscription, signature, headBlock)
                .thenCompose(existingSub -> {
                    if (existingSub.exists()) {
                        // 3. If so, evict relevant run from pending
                        log.info(
                            "Delegated subscription exists on-chain with ID: {}, tracked locally: {}",
                            existingSub.subscriptionId(),
                            subscriptions.containsKey(existingSub.subscriptionId())
                        );

                        // Evict current delegate runs from pending
                        // The interval might not be known at this point, so we use a placeholder or a convention.
                        // Using 0 assumes we want to clear any pending run for this delegated ID.
                        SubscriptionRunKey key = new SubscriptionRunKey(
                            new DelegatedSubscriptionId(subscription.getClient(), signature.nonce()),
                            0
                        );
                        synchronized (this) {
                            if (pendingTxs.remove(key) != null) {
                                log.info("Evicted past pending subscription tx for run: {}", key);
                            }
                            if (txAttempts.remove(key) != null) {
                                log.info("Evicted past pending subscription attempts for run: {}", key);
                            }
                        }
                        return CompletableFuture.completedFuture(null); // End of this path
                    } else {
                        // 4. If not, verify that recovered signer == delegated signer and track it
                        return verifyAndTrackDelegation(subscription, signature, headBlock, requestDTO.getData());
                    }
                })
        );
    }

    private CompletableFuture<Void> verifyAndTrackDelegation(
        SubscriptionDTO subscription,
        SignatureParamsDTO signature,
        long headBlock,
        Map<String, Object> data
    ) {
        // Run both async calls in parallel
        CompletableFuture<String> recoveredSignerFuture = coordinator.recoverDelegateeSigner(subscription, signature);
        CompletableFuture<String> delegatedSignerFuture = coordinator.getDelegatedSigner(subscription, headBlock);

        return recoveredSignerFuture
            .thenCombine(delegatedSignerFuture, (recoveredSigner, delegatedSigner) -> {
                if (!recoveredSigner.equalsIgnoreCase(delegatedSigner)) {
                    // If signers don't match, throw an exception to fail the future
                    throw new IllegalStateException(
                        "Subscription signer mismatch. Recovered: " + recoveredSigner + ", Delegated: " + delegatedSigner
                    );
                }
                // If they match, return the verified signer for the next step
                return recoveredSigner;
            })
            .thenAccept(verifiedSigner -> {
                // 5. If verified, adds subscription to _delegate_subscriptions
                log.debug("Successfully verified delegated signer: {}", verifiedSigner);
                DelegatedSubscriptionId subId = new DelegatedSubscriptionId(subscription.getClient(), signature.nonce());
                DelegatedSubscriptionData delegatedSubscriptionData = new DelegatedSubscriptionData(subscription, signature, data);
                delegateSubscriptions.put(subId, delegatedSubscriptionData);
                log.info("Tracked new delegate subscription: {}", subId);
            });
    }

    /**
     * Core processing loop, runs every 100ms.
     */
    @Scheduled(fixedDelay = 100)
    public void processActiveSubscriptions() {
        pruneFailedTxs();

        // Process regular subscriptions
        subscriptions.forEach((subId, subscription) -> {
            shouldProcess(new OnchainSubscriptionId(subId), subscription, false).thenAccept(should -> {
                if (should) {
                    processSubscription(new OnchainSubscriptionId(subId), subscription, false, null);
                }
            });
        });

        // Process delegated subscriptions
        delegateSubscriptions.forEach((delegateSubId, params) -> {
            shouldProcess(delegateSubId, params.subscription(), true).thenAccept(should -> {
                if (should) {
                    processSubscription(delegateSubId, params.subscription(), true, params);
                }
            });
        });
    }

    private CompletableFuture<Boolean> shouldProcess(SubscriptionIdentifier subId, SubscriptionDTO subscription, boolean isDelegated) {
        if (!subscription.isActive()) {
            return CompletableFuture.completedFuture(false);
        }

        long interval = subscription.getInterval();
        SubscriptionRunKey runKey = new SubscriptionRunKey(subId, interval);

        if (pendingTxs.containsKey(runKey)) {
            return CompletableFuture.completedFuture(false); // Already processing
        }

        if (txAttempts.getOrDefault(runKey, new AtomicInteger(0)).get() >= 3) {
            log.warn("Subscription {} has exceeded max retries for interval {}.", subId, interval);
            return CompletableFuture.completedFuture(false);
        }

        if (!isDelegated) {
            // For non-delegated, check if already responded
            return coordinator
                .getNodeHasDeliveredResponse(subscription.getId(), interval, wallet.getAddress(), null)
                .thenApply(hasResponded -> {
                    if (hasResponded) {
                        subscription.setNodeReplied(interval);
                    }
                    return !hasResponded;
                });
        }

        return CompletableFuture.completedFuture(true);
    }

    @Async
    public void pruneFailedTxs() {
        pendingTxs.forEach((runKey, txHash) -> {
            if (!BLOCKED_TX.equals(txHash)) {
                coordinator
                    .getTxSuccess(txHash)
                    .thenAccept(txReceipt -> {
                        if (txReceipt != null && !txReceipt.success()) {
                            synchronized (this) {
                                int attempts = txAttempts.computeIfAbsent(runKey, k -> new AtomicInteger(0)).incrementAndGet();
                                if (attempts < 3) {
                                    pendingTxs.remove(runKey);
                                    log.info("Evicted failed tx for {}, retries: {}", runKey, attempts);
                                } else {
                                    log.error("Max retries reached for {}. It will be blocked.", runKey);
                                }
                            }
                        }
                    });
            }
        });
    }

    @Async
    public void processSubscription(
        SubscriptionIdentifier id,
        SubscriptionDTO subscription,
        boolean delegated,
        DelegatedSubscriptionData delegatedParams
    ) {
        long interval = subscription.getInterval();
        SubscriptionRunKey runKey = new SubscriptionRunKey(id, interval);

        log.info("Processing subscription: id={}, interval={}, delegated={}", id, interval, delegated);

        // Block further processing for this run
        pendingTxs.put(runKey, BLOCKED_TX);

        // Execute containers and process the result
        executeOnContainers(subscription, delegated, delegatedParams)
            .thenCompose(results -> {
                if (results.isEmpty() || results.get(results.size() - 1) instanceof ContainerErrorDTO) {
                    log.error("Container execution failed for {}: {}", runKey, results);
                    pendingTxs.remove(runKey); // Unblock for retry
                    if (subscription.isCallback()) {
                        stopTracking(id, delegated);
                    }
                    return CompletableFuture.completedFuture(null); // End chain
                }

                ContainerOutputDTO lastResult = (ContainerOutputDTO) results.get(results.size() - 1);
                log.info("Container execution succeeded for {}", runKey);

                // Serialize output and deliver transaction
                SerializedOutput serialized = serializeContainerOutput(lastResult);
                return deliver(subscription, delegated, delegatedParams, serialized);
            })
            .thenAccept(txHash -> {
                if (txHash != null) {
                    pendingTxs.put(runKey, txHash);
                    log.info("Sent tx for {}: {}", runKey, txHash);
                }
            })
            .exceptionally(e -> {
                log.error("Failed to process subscription {}: {}", runKey, e.getMessage());
                pendingTxs.remove(runKey); // Unblock for retry
                if (subscription.isCallback()) {
                    stopTracking(id, delegated);
                }
                return null;
            });
    }

    private CompletableFuture<List<ContainerResultDTO>> executeOnContainers(
        SubscriptionDTO subscription,
        boolean delegated,
        DelegatedSubscriptionData delegatedParams
    ) {
        CompletableFuture<ComputationInputDTO> computationInputFuture;
        if (delegated) {
            computationInputFuture = CompletableFuture.completedFuture(
                ComputationInputDTO.builder()
                    .source(ComputationLocation.OFF_CHAIN.name())
                    .destination(ComputationLocation.ON_CHAIN.name())
                    .data(delegatedParams.data())
                    .build()
            );
        } else {
            computationInputFuture = coordinator
                .getContainerInputs(subscription, subscription.getInterval())
                .thenApply(inputHex ->
                    ComputationInputDTO.builder()
                        .source(ComputationLocation.ON_CHAIN.name())
                        .destination(ComputationLocation.ON_CHAIN.name())
                        .data(Map.of("hex_data", inputHex))
                        .build()
                );
        }

        return computationInputFuture.thenCompose(computationInput ->
            computationService.processChainProcessorComputation(
                UUID.randomUUID(),
                computationInput,
                List.of(subscription.getContainerId()),
                subscription.hasVerifier()
            )
        );
    }

    private CompletableFuture<String> deliver(
        SubscriptionDTO subscription,
        boolean delegated,
        DelegatedSubscriptionData delegatedParams,
        SerializedOutput serializedOutput
    ) {
        if (delegated) {
            return wallet.deliverComputeDelegatee(
                subscription,
                delegatedParams.signature(),
                serializedOutput.input(),
                serializedOutput.output(),
                serializedOutput.proof()
            );
        } else {
            return wallet.deliverCompute(subscription, serializedOutput.input(), serializedOutput.output(), serializedOutput.proof(), null);
        }
    }

    private void stopTracking(SubscriptionIdentifier subscriptionId, boolean delegated) {
        if (delegated) {
            delegateSubscriptions.remove((DelegatedSubscriptionId) subscriptionId);
        } else {
            subscriptions.remove(((OnchainSubscriptionId) subscriptionId).id());
        }

        pendingTxs.keySet().removeIf(key -> key.subscriptionId().equals(subscriptionId));
        txAttempts.keySet().removeIf(key -> key.subscriptionId().equals(subscriptionId));

        log.info("Stopped tracking subscription: {}", subscriptionId);
    }

    private SerializedOutput serializeContainerOutput(ContainerOutputDTO output) {
        // Simplified serialization.
        String outputString = output.getOutput().toString();
        byte[] inputBytes = new byte[0];
        byte[] outputBytes = outputString.getBytes();
        byte[] proofBytes = new byte[0];
        return new SerializedOutput(inputBytes, outputBytes, proofBytes);
    }

    private record SerializedOutput(byte[] input, byte[] output, byte[] proof) {}
}

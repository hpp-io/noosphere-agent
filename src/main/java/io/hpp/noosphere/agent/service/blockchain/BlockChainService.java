package io.hpp.noosphere.agent.service.blockchain;

import static io.hpp.noosphere.agent.service.util.CommonUtil.decodeInputDataToString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hpp.noosphere.agent.service.ComputationService;
import io.hpp.noosphere.agent.service.ContainerLookupService;
import io.hpp.noosphere.agent.service.blockchain.dto.*;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3SubscriptionBatchReaderService;
import io.hpp.noosphere.agent.service.dto.*;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.mapper.SubscriptionMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

    private record ShouldProcessResult(boolean should, byte[] commitment) {}

    private final Web3j web3j;
    private final CoordinatorService coordinator;
    private final WalletService wallet;
    private final ComputationService computationService;
    private final ContainerLookupService containerLookupService;
    private final Web3SubscriptionBatchReaderService batchReaderService;
    private final SubscriptionMapper subscriptionMapper;

    // State management using thread-safe collections
    private final Map<OnchainSubscriptionId, OnchainRequestDTO> onChainRequests = new ConcurrentHashMap<>();
    private final Map<DelegatedSubscriptionId, DelegatedSubscriptionData> delegateSubscriptions = new ConcurrentHashMap<>();
    private final Map<SubscriptionRunKey, String> pendingTxs = new ConcurrentHashMap<>();
    private final Map<SubscriptionRunKey, AtomicInteger> txAttempts = new ConcurrentHashMap<>();

    public BlockChainService(
        Web3j web3j,
        CoordinatorService coordinator,
        WalletService wallet,
        ComputationService computationService,
        ContainerLookupService containerLookupService,
        Web3SubscriptionBatchReaderService batchReaderService,
        SubscriptionMapper subscriptionMapper
    ) {
        this.web3j = web3j;
        this.coordinator = coordinator;
        this.wallet = wallet;
        this.computationService = computationService;
        this.containerLookupService = containerLookupService;
        this.batchReaderService = batchReaderService;
        this.subscriptionMapper = subscriptionMapper;
    }

    /**
     * Tracks incoming on-chain messages.
     */
    @Async
    public CompletableFuture<Void> processIncomingRequest(BaseRequestDTO request) {
        if (request instanceof OnchainRequestDTO onchainRequestDTO) {
            log.info(
                "Processing ONCHAIN request - subscriptionId: {}, interval: {}",
                onchainRequestDTO.getSubscription().getId(),
                onchainRequestDTO.getCommitment().interval
            );
            ProcessOnchainRequest(onchainRequestDTO);
            return CompletableFuture.completedFuture(null);
        } else if (request instanceof DelegatedRequestDTO delegatedRequestDTO) {
            log.info(
                "Processing DELEGATED request - subscriptionId: {}, client: {}",
                delegatedRequestDTO.getSubscription().getId(),
                delegatedRequestDTO.getSubscription().getClient()
            );
            return ProcessDelegatedRequest(delegatedRequestDTO);
        } else {
            log.error("Unknown request type to track: {}", request);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown request type"));
        }
    }

    private void ProcessOnchainRequest(OnchainRequestDTO requestDTO) {
        onChainRequests.put(
            new OnchainSubscriptionId(requestDTO.getSubscription().getId(), requestDTO.getCommitment().interval.longValue()),
            requestDTO
        );
        log.info(
            "Tracked new subscription! id={}, interval={}, total={}",
            requestDTO.getSubscription().getId(),
            requestDTO.getCommitment().interval,
            onChainRequests.size()
        );
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
                        log.info("Delegated subscription exists on-chain with ID: {}", existingSub.subscriptionId());

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
     * Refreshes subscription cache from blockchain every 30 seconds using batch reading.
     * This detects cancelled or modified subscriptions efficiently with minimal RPC calls.
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void refreshSubscriptionCache() {
        if (onChainRequests.isEmpty()) {
            return;
        }

        try {
            // Get all subscription IDs to refresh
            List<Long> subIds = onChainRequests
                .keySet()
                .stream()
                .map(OnchainSubscriptionId::id)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

            if (subIds.isEmpty()) {
                return;
            }

            int minId = subIds.get(0).intValue();
            int maxId = subIds.get(subIds.size() - 1).intValue();
            long currentBlock = web3j.ethBlockNumber().send().getBlockNumber().longValue();

            log.debug("Batch refreshing {} subscriptions (IDs {}-{}) from blockchain", subIds.size(), minId, maxId);

            // Batch read all subscriptions
            batchReaderService
                .getSubscriptions(minId, maxId, currentBlock)
                .thenAccept(subscriptions -> {
                    // Create a map of subscription ID -> subscription for quick lookup
                    // Batch reader returns subscriptions in order from minId to maxId
                    Map<Long, SubscriptionDTO> freshSubsMap = new HashMap<>();
                    for (int i = 0; i < subscriptions.size(); i++) {
                        long subscriptionId = minId + i;
                        var contractSub = subscriptions.get(i);
                        SubscriptionDTO dto = subscriptionMapper.toDto(subscriptionId, contractSub, containerLookupService);
                        freshSubsMap.put(subscriptionId, dto);
                    }

                    // Update cache for each tracked subscription
                    onChainRequests.forEach((subId, onchainRequest) -> {
                        SubscriptionDTO freshSub = freshSubsMap.get(subId.id());

                        if (freshSub == null || freshSub.isCancelled()) {
                            log.info("Subscription {} no longer exists or is cancelled. Will stop tracking on next cycle.", subId);
                            // Mark for removal by updating with a cancelled subscription
                            if (onchainRequest.getSubscription() != null) {
                                onchainRequest.getSubscription().setClient("0x0000000000000000000000000000000000000000");
                            }
                        } else {
                            // Update cache with fresh data
                            onchainRequest.setSubscription(freshSub);
                            log.debug("Refreshed subscription {} cache from blockchain", subId);
                        }
                    });

                    log.info("Batch refreshed {} subscriptions from blockchain", subIds.size());
                })
                .exceptionally(ex -> {
                    log.warn("Failed to batch refresh subscriptions from blockchain: {}", ex.getMessage());
                    return null;
                });
        } catch (Exception ex) {
            log.error("Error during subscription cache refresh: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Core processing loop, runs every 5 seconds.
     */
    @Scheduled(fixedDelayString = "${application.noosphere.chain.processing-interval}")
    public void processActiveSubscriptions() {
        pruneFailedTxs();
        // Process regular subscriptions
        onChainRequests.forEach((subId, onchainRequest) -> {
            shouldProcessOnChain(subId, onchainRequest.getSubscription()).thenAccept(result -> {
                if (result.should()) {
                    processSubscription(subId, onchainRequest.getSubscription(), false, null, result.commitment());
                } else {
                    stopTracking(subId, false);
                }
            });
        });

        // Process delegated subscriptions
        delegateSubscriptions.forEach((delegateSubId, params) -> {
            shouldProcessDelegated(delegateSubId, params.subscription(), true).thenAccept(result -> {
                if (result.should()) {
                    processSubscription(delegateSubId, params.subscription(), true, params, null);
                }
            });
        });
    }

    private CompletableFuture<ShouldProcessResult> shouldProcessOnChain(SubscriptionIdentifier subId, SubscriptionDTO subscription) {
        // Ensure the identifier is the correct type before casting
        if (!(subId instanceof OnchainSubscriptionId onchainSubId)) {
            log.warn("shouldProcessOnChain called with incorrect subscription identifier type: {}", subId.getClass().getName());
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
        }

        // Check if subscription is cancelled (empty/zero values from blockchain)
        if (subscription.isCancelled()) {
            log.info("Subscription {} is cancelled (client address is zero or empty). Stopping processing.", subId);
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
        }

        long id = onchainSubId.id();

        long interval = onchainSubId.interval();
        SubscriptionRunKey runKey = new SubscriptionRunKey(onchainSubId, interval);

        if (pendingTxs.containsKey(runKey)) {
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null)); // Already processing
        }

        if (txAttempts.getOrDefault(runKey, new AtomicInteger(0)).get() >= 3) {
            log.warn("Subscription {} has exceeded max retries for interval {}.", subId, interval);
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
        }
        log.debug(
            "scription Id: {}, interval: {} containerId: {}",
            id,
            interval,
            containerLookupService.getContainers(subscription.getContainerId())
        );
        // For non-delegated, check if already responded
        return coordinator
            .getNodeHasDeliveredResponse(id, interval, wallet.getAddress(), null)
            .thenCompose(hasResponded -> {
                if (hasResponded) {
                    subscription.setNodeReplied(interval);
                    return CompletableFuture.completedFuture(new ShouldProcessResult(false, null)); // Already responded, do not process
                }
                // If not responded, check if a valid commitment exists for this interval
                return coordinator
                    .hasRequestCommitments(id, interval)
                    .thenCompose(hasCommitment -> {
                        if (hasCommitment) {
                            // If commitment exists, fetch it to pass it to the processing step.
                            return coordinator
                                .getCommitment(id, interval)
                                .thenApply(commitment -> {
                                    log.debug(
                                        "sub scription Id: {}, containerId: {}, hasCommitment: {}, commitment: {}",
                                        subscription.getId(),
                                        containerLookupService.getContainers(subscription.getContainerId()),
                                        hasCommitment,
                                        commitment
                                    );
                                    // If the commitment is null (meaning no valid commitment was found on-chain),
                                    // we should not process.
                                    boolean shouldProcess = commitment != null;
                                    byte[] encodedCommitment = shouldProcess ? coordinator.encodeCommitment(commitment) : null;
                                    return new ShouldProcessResult(shouldProcess, encodedCommitment);
                                });
                        } else {
                            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
                        }
                    });
            });
    }

    private CompletableFuture<ShouldProcessResult> shouldProcessDelegated(
        SubscriptionIdentifier subId,
        SubscriptionDTO subscription,
        boolean isDelegated
    ) {
        if (!subscription.isActive()) {
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
        }

        // Check if subscription is cancelled (empty/zero values from blockchain)
        if (subscription.isCancelled()) {
            log.info("Subscription {} is cancelled (client address is zero or empty). Stopping processing.", subId);
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
        }

        long interval = subscription.getInterval();
        SubscriptionRunKey runKey = new SubscriptionRunKey(subId, interval);

        if (pendingTxs.containsKey(runKey)) {
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null)); // Already processing
        }

        if (txAttempts.getOrDefault(runKey, new AtomicInteger(0)).get() >= 3) {
            log.warn("Subscription {} has exceeded max retries for interval {}.", subId, interval);
            return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
        }
        log.debug(
            "scription Id: {}, interval: {} containerId: {}",
            subscription.getId(),
            subscription.getInterval(),
            containerLookupService.getContainers(subscription.getContainerId())
        );
        if (!isDelegated) {
            // For non-delegated, check if already responded
            return coordinator
                .getNodeHasDeliveredResponse(subscription.getId(), interval, wallet.getAddress(), null)
                .thenCompose(hasResponded -> {
                    if (hasResponded) {
                        subscription.setNodeReplied(interval);
                        return CompletableFuture.completedFuture(new ShouldProcessResult(false, null)); // Already responded, do not process
                    }
                    // If not responded, check if a valid commitment exists for this interval
                    return coordinator
                        .hasRequestCommitments(subscription.getId(), interval)
                        .thenCompose(hasCommitment -> {
                            if (hasCommitment) {
                                // If commitment exists, fetch it to pass it to the processing step.
                                return coordinator
                                    .getCommitment(subscription.getId(), interval)
                                    .thenApply(commitment -> {
                                        log.debug(
                                            "sub scription Id: {}, containerId: {}, hasConnmitment: {}",
                                            subscription.getId(),
                                            containerLookupService.getContainers(subscription.getContainerId()),
                                            hasCommitment
                                        );
                                        return new ShouldProcessResult(true, coordinator.encodeCommitment(commitment));
                                    });
                            } else {
                                // If no commitment, no need to process.
                                return CompletableFuture.completedFuture(new ShouldProcessResult(false, null));
                            }
                        });
                });
        }

        return CompletableFuture.completedFuture(new ShouldProcessResult(true, null));
    }

    @Async
    public void pruneFailedTxs() {
        pendingTxs.forEach((runKey, txHash) -> {
            // "0xblocked"는 이미 처리 중이거나 영구 실패한 작업이므로 건너뜀
            if (!BLOCKED_TX.equals(txHash)) {
                // 재시도 횟수가 이미 3번 이상이면 더 이상 확인하지 않음
                if (txAttempts.getOrDefault(runKey, new AtomicInteger(0)).get() >= 3) {
                    return;
                }

                coordinator
                    .getTxSuccess(txHash)
                    .thenAccept(txReceipt -> {
                        if (txReceipt != null && !txReceipt.success()) {
                            synchronized (this) {
                                int attempts = txAttempts.computeIfAbsent(runKey, k -> new AtomicInteger(0)).incrementAndGet();
                                if (attempts >= 3) {
                                    log.error("Max retries reached for {}. It will be blocked permanently.", runKey);
                                    pendingTxs.put(runKey, BLOCKED_TX); // 영구적으로 차단
                                } else {
                                    pendingTxs.remove(runKey);
                                    log.info("Evicted failed tx for {}, retries: {}", runKey, attempts);
                                }
                            }
                        }
                    });
            }
        });
    }

    @Async
    public void processSubscription(
        SubscriptionIdentifier subId,
        SubscriptionDTO subscription,
        boolean delegated,
        DelegatedSubscriptionData delegatedParams,
        byte[] commitment
    ) {
        SubscriptionRunKey runKey;
        long interval;
        if (!delegated) {
            if (!(subId instanceof OnchainSubscriptionId onchainSubId)) {
                log.warn("processSubscription called with incorrect subscription identifier type: {}", subId.getClass().getName());
                return;
            }
            interval = onchainSubId.interval();
        } else {
            if (!(subId instanceof DelegatedSubscriptionId delegatedSubscriptionId)) {
                log.warn("processSubscription called with incorrect subscription identifier type: {}", subId.getClass().getName());
                return;
            }
            interval = delegatedSubscriptionId.nonce();
        }
        runKey = new SubscriptionRunKey(subId, interval);
        // Block further processing for this run
        pendingTxs.put(runKey, BLOCKED_TX);

        // Execute containers and process the result
        executeOnContainers(subscription, interval, delegated, delegatedParams, extractRequestIdFromCommitment(commitment), commitment)
            .thenCompose(results -> {
                if (results.isEmpty() || results.get(results.size() - 1) instanceof ContainerErrorDTO) {
                    log.error("Container execution failed for {}: {}", runKey, results);
                    pendingTxs.remove(runKey); // Unblock for retry
                    return CompletableFuture.completedFuture(null); // End chain
                }

                ContainerOutputDTO lastResult = (ContainerOutputDTO) results.get(results.size() - 1);
                log.info("Container execution succeeded for {}", runKey);
                // Serialize output and deliver transaction
                SerializedOutput serialized = serializeContainerOutput(lastResult);
                return deliver(subscription, interval, delegated, delegatedParams, serialized, commitment).exceptionally(ex -> {
                    log.error("Failed to deliver transaction for {}: {}", runKey, ex.getMessage());
                    stopTracking(subId, delegated);
                    return null; // Stop the chain
                });
            })
            .thenAccept(txHash -> {
                if (txHash != null) {
                    log.info("Sent tx for {}: {}", runKey, txHash);
                    stopTracking(subId, delegated);
                }
            });
    }

    private CompletableFuture<List<ContainerResultDTO>> executeOnContainers(
        SubscriptionDTO subscription,
        long interval,
        boolean delegated,
        DelegatedSubscriptionData delegatedParams,
        byte[] requestId,
        byte[] commitment
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
                .getContainerInputs(subscription, interval)
                .thenApply(inputHex ->
                    ComputationInputDTO.builder()
                        .source(ComputationLocation.ON_CHAIN.name())
                        .destination(ComputationLocation.ON_CHAIN.name())
                        .data(Map.of("hex_data", inputHex))
                        .build()
                );
        }

        return computationInputFuture.thenCompose(computationInput -> {
            log.debug(
                "Processing subscription {} interval {} containerId {} input.data (decoded): {}",
                subscription.getId(),
                interval,
                containerLookupService.getContainers(subscription.getContainerId()),
                decodeInputDataToString(computationInput.getData())
            );
            log.debug(
                "Subscription {} hasVerifier={} verifier={} delegated={}",
                subscription.getId(),
                subscription.hasVerifier(),
                subscription.getVerifier(),
                delegated
            );
            return computationService.processChainProcessorComputation(
                UUID.randomUUID(),
                computationInput,
                containerLookupService.getContainers(subscription.getContainerId()),
                subscription.hasVerifier(),
                requestId,
                commitment,
                delegated ? delegatedParams.subscription() : subscription
            );
        });
    }

    private CompletableFuture<String> deliver(
        SubscriptionDTO subscription,
        long interval,
        boolean delegated,
        DelegatedSubscriptionData delegatedParams,
        SerializedOutput serializedOutput,
        byte[] commitment
    ) {
        log.info(
            "deliver() called - subscriptionId: {}, interval: {}, delegated: {}, hasVerifier: {}, verifier: {}",
            subscription.getId(),
            interval,
            delegated,
            subscription.hasVerifier(),
            subscription.getVerifier()
        );

        if (!delegated) {
            log.info("Commitment data - length: {}, isNull: {}", commitment != null ? commitment.length : 0, commitment == null);
            if (commitment == null || commitment.length == 0) {
                log.error("CRITICAL: Commitment data is null or empty for subscription {}, interval {}", subscription.getId(), interval);
            }
        }

        if (delegated) {
            log.info("Using DELEGATED path - calling reportDelegatedComputeResult");
            return wallet.deliverComputeDelegatee(
                subscription,
                delegatedParams.signature(),
                serializedOutput.input(),
                serializedOutput.output(),
                serializedOutput.proof()
            );
        } else {
            log.info("Using NORMAL path - calling reportComputeResult with proof");
            return wallet.deliverCompute(
                subscription,
                interval,
                serializedOutput.input(),
                serializedOutput.output(),
                serializedOutput.proof(),
                commitment
            );
        }
    }

    private void stopTracking(SubscriptionIdentifier subscriptionId, boolean delegated) {
        if (delegated) {
            delegateSubscriptions.remove((DelegatedSubscriptionId) subscriptionId);
        } else {
            onChainRequests.remove((OnchainSubscriptionId) subscriptionId);
        }

        pendingTxs.keySet().removeIf(key -> key.subscriptionId().equals(subscriptionId));
        txAttempts.keySet().removeIf(key -> key.subscriptionId().equals(subscriptionId));

        log.info("Stopped tracking subscription: {}", subscriptionId);
    }

    private SerializedOutput serializeContainerOutput(ContainerOutputDTO output) {
        byte[] inputBytes;
        // For String inputs, use the raw string bytes to match proof container hashing
        // For complex objects (Map, List, etc.), use JSON serialization
        if (output.getInputs() instanceof String) {
            String inputStr = (String) output.getInputs();
            inputBytes = inputStr.getBytes(StandardCharsets.UTF_8);
            log.info(
                "Using raw string bytes for input: length={}, sha3=0x{}",
                inputBytes.length,
                org.web3j.utils.Numeric.toHexStringNoPrefix(org.web3j.crypto.Hash.sha3(inputBytes))
            );
        } else {
            try {
                // Use ObjectMapper to correctly serialize the input object (Map, List, etc.) to JSON bytes.
                inputBytes = new ObjectMapper().writeValueAsBytes(output.getInputs());
                log.info(
                    "Serialized input using JSON: length={}, input type={}, sha3=0x{}",
                    inputBytes.length,
                    output.getInputs().getClass().getSimpleName(),
                    org.web3j.utils.Numeric.toHexStringNoPrefix(org.web3j.crypto.Hash.sha3(inputBytes))
                );
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize container input, falling back to toString()", e);
                inputBytes = output.getInputs().toString().getBytes(StandardCharsets.UTF_8);
            }
        }
        byte[] outputBytes;
        // For String outputs, use the raw string bytes to match proof container hashing
        // For complex objects (Map, List, etc.), use JSON serialization
        if (output.getOutput() instanceof String) {
            String outputStr = (String) output.getOutput();
            outputBytes = outputStr.getBytes(StandardCharsets.UTF_8);
            log.info(
                "Using raw string bytes for output: length={}, sha3=0x{}",
                outputBytes.length,
                org.web3j.utils.Numeric.toHexStringNoPrefix(org.web3j.crypto.Hash.sha3(outputBytes))
            );
        } else {
            try {
                // Use ObjectMapper to correctly serialize the output object (Map, List, etc.) to JSON bytes.
                outputBytes = new ObjectMapper().writeValueAsBytes(output.getOutput());
                log.info(
                    "Serialized output using JSON: length={}, output type={}, sha3=0x{}",
                    outputBytes.length,
                    output.getOutput().getClass().getSimpleName(),
                    org.web3j.utils.Numeric.toHexStringNoPrefix(org.web3j.crypto.Hash.sha3(outputBytes))
                );
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize container output, falling back to toString()", e);
                outputBytes = output.getOutput().toString().getBytes(StandardCharsets.UTF_8);
            }
        }

        byte[] proofBytes;
        if (output.getProof() == null || output.getProof().isEmpty()) {
            proofBytes = new byte[0];
        } else {
            String proofStr = output.getProof();
            // If proof is a hex string (starts with 0x), decode it properly
            if (proofStr.startsWith("0x") || proofStr.startsWith("0X")) {
                try {
                    proofBytes = org.web3j.utils.Numeric.hexStringToByteArray(proofStr);
                    log.debug("Decoded proof from hex string, length: {}", proofBytes.length);
                } catch (Exception e) {
                    log.error("Failed to decode proof hex string: {}", proofStr, e);
                    proofBytes = proofStr.getBytes(StandardCharsets.UTF_8);
                }
            } else {
                // Try to parse as JSON and ABI-encode it
                try {
                    proofBytes = parseAndEncodeProofJson(proofStr);
                    log.info("Successfully parsed and ABI-encoded proof JSON, length: {}", proofBytes.length);
                } catch (Exception e) {
                    log.error("Failed to parse proof as JSON, falling back to UTF-8 encoding: {}", e.getMessage());
                    proofBytes = proofStr.getBytes(StandardCharsets.UTF_8);
                }
            }
        }
        return new SerializedOutput(inputBytes, outputBytes, proofBytes);
    }

    /**
     * Parses proof JSON and ABI-encodes it in the format expected by ImmediateFinalizeVerifier.
     * Expected format: (bytes32 requestId, bytes32 commitmentHash, bytes32 inputHash,
     *                   bytes32 resultHash, address nodeAddress, uint256 timestamp, bytes signature)
     */
    private byte[] parseAndEncodeProofJson(String proofJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var proofMap = mapper.readValue(proofJson, Map.class);

        // Extract fields from JSON
        String requestId = (String) proofMap.get("requestId");
        String commitmentHash = (String) proofMap.get("commitmentHash");
        String inputHash = (String) proofMap.get("inputHash");
        String resultHash = (String) proofMap.get("resultHash");
        String nodeAddress = (String) proofMap.get("nodeAddress");
        Object timestampObj = proofMap.get("timestamp");
        String signature = (String) proofMap.get("signature");

        if (
            requestId == null ||
            commitmentHash == null ||
            inputHash == null ||
            resultHash == null ||
            nodeAddress == null ||
            timestampObj == null ||
            signature == null
        ) {
            throw new IllegalArgumentException("Proof JSON is missing required fields");
        }

        log.debug("Parsing proof JSON - requestId: {}, nodeAddress: {}, timestamp: {}", requestId, nodeAddress, timestampObj);

        // Convert timestamp to BigInteger
        BigInteger timestamp;
        if (timestampObj instanceof Number) {
            timestamp = BigInteger.valueOf(((Number) timestampObj).longValue());
        } else {
            timestamp = new BigInteger(timestampObj.toString());
        }

        // Decode hex strings to bytes
        byte[] requestIdBytes = org.web3j.utils.Numeric.hexStringToByteArray(requestId);
        byte[] commitmentHashBytes = org.web3j.utils.Numeric.hexStringToByteArray(commitmentHash);
        byte[] inputHashBytes = org.web3j.utils.Numeric.hexStringToByteArray(inputHash);
        byte[] resultHashBytes = org.web3j.utils.Numeric.hexStringToByteArray(resultHash);
        byte[] signatureBytes = org.web3j.utils.Numeric.hexStringToByteArray(signature);

        // ABI-encode the proof data
        // Format: (bytes32, bytes32, bytes32, bytes32, address, uint256, bytes)
        org.web3j.abi.datatypes.Function dummyFunction = new org.web3j.abi.datatypes.Function(
            "dummy",
            Arrays.asList(
                new org.web3j.abi.datatypes.generated.Bytes32(requestIdBytes),
                new org.web3j.abi.datatypes.generated.Bytes32(commitmentHashBytes),
                new org.web3j.abi.datatypes.generated.Bytes32(inputHashBytes),
                new org.web3j.abi.datatypes.generated.Bytes32(resultHashBytes),
                new org.web3j.abi.datatypes.Address(nodeAddress),
                new org.web3j.abi.datatypes.generated.Uint256(timestamp),
                new org.web3j.abi.datatypes.DynamicBytes(signatureBytes)
            ),
            Collections.emptyList()
        );

        // Encode and strip the function selector (first 4 bytes)
        String encoded = org.web3j.abi.FunctionEncoder.encode(dummyFunction);
        byte[] encodedBytes = org.web3j.utils.Numeric.hexStringToByteArray(encoded);

        // Remove the function selector (first 4 bytes) to get just the parameters
        byte[] result = new byte[encodedBytes.length - 4];
        System.arraycopy(encodedBytes, 4, result, 0, result.length);

        log.info(
            "ABI-encoded proof: requestId={}, commitmentHash={}, inputHash={}, resultHash={}, nodeAddress={}, timestamp={}, encoded length={}",
            requestId,
            commitmentHash,
            inputHash,
            resultHash,
            nodeAddress,
            timestamp,
            result.length
        );
        log.info("IMPORTANT: Proof contains resultHash={} - this must match the hash of the output being sent", resultHash);

        return result;
    }

    private record SerializedOutput(byte[] input, byte[] output, byte[] proof) {}

    /**
     * Extracts the requestId (first 32 bytes) from the ABI-encoded commitment data.
     *
     * @param commitment The full commitment byte array.
     * @return The 32-byte requestId.
     */
    private byte[] extractRequestIdFromCommitment(byte[] commitment) {
        if (commitment == null || commitment.length < 32) {
            log.error("Invalid or too short commitment data provided to extract requestId.");
            return new byte[32]; // Return an empty bytes32 array to avoid nulls
        }
        byte[] requestId = new byte[32];
        System.arraycopy(commitment, 0, requestId, 0, 32);
        return requestId;
    }
}

package io.hpp.noosphere.agent.service.blockchain;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.contracts.Router;
import io.hpp.noosphere.agent.contracts.SubscriptionBatchReader;
import io.hpp.noosphere.agent.service.ContainerLookupService;
import io.hpp.noosphere.agent.service.NoosphereConfigService;
import io.hpp.noosphere.agent.service.RequestValidatorService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3RouterService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3SubscriptionBatchReaderService;
import io.hpp.noosphere.agent.service.dto.OnchainRequestDTO;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import io.hpp.noosphere.agent.service.mapper.SubscriptionMapper;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Numeric;

@Service
public class BlockchainListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(BlockchainListener.class);

    private final Web3j web3j;
    private final Web3RouterService web3Router;
    private final Web3SubscriptionBatchReaderService web3BatchReader;
    private final RequestValidatorService requestValidatorService;
    private final BlockChainService blockChainService;
    private final ApplicationProperties.NoosphereConfig.Chain properties;
    private final ContainerLookupService containerLookupService;
    private final SubscriptionMapper subscriptionMapper;

    private final AtomicLong lastSyncedBlock = new AtomicLong(0);
    private final AtomicLong lastSubscriptionId = new AtomicLong(0);
    private final AtomicBoolean isSnapshotSyncing = new AtomicBoolean(false);
    private Disposable requestStartedSubscription;

    public BlockchainListener(
        Web3j web3j,
        Web3RouterService web3Router,
        Web3SubscriptionBatchReaderService web3BatchReader,
        RequestValidatorService requestValidatorService,
        BlockChainService blockChainService,
        NoosphereConfigService noosphereConfigService,
        ContainerLookupService containerLookupService,
        SubscriptionMapper subscriptionMapper
    ) {
        this.web3j = web3j;
        this.web3Router = web3Router;
        this.web3BatchReader = web3BatchReader;
        this.requestValidatorService = requestValidatorService;
        this.blockChainService = blockChainService;
        this.properties = noosphereConfigService.getActiveConfig().getChain();
        this.containerLookupService = containerLookupService;
        this.subscriptionMapper = subscriptionMapper;
        log.info("Initialized ChainListenerService");
    }

    /**
     * Application startup logic.
     */
    @Override
    @Async
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("ChainListener starting setup...");
        try {
            Long headBlock = web3j.ethBlockNumber().send().getBlockNumber().longValue() - properties.getTrailHeadBlocks();
            lastSyncedBlock.set(headBlock);
            lastSubscriptionId.set(properties.getSnapshotSync().getStartingSubId());

            log.info("Started snapshot sync up to block {}", headBlock);
            //            isSnapshotSyncing.set(true);
            //            snapshotSync(headBlock).join(); // Wait for snapshot sync to complete
            //            isSnapshotSyncing.set(false);

            long headSubId = web3Router.getLastSubscriptionId(headBlock).join().longValue();
            lastSubscriptionId.set(headSubId);

            log.info("Finished snapshot sync. Last synced block: {}, Last sub ID: {}", headBlock, headSubId);

            // Start listening for real-time events after initial sync
            startListeningForEvents();
        } catch (Exception e) {
            log.error("Fatal error during initial snapshot sync. Shutting down.", e);
            // In a real app, you might want to shut down the application context here.
        }
    }

    /**
     * Core processing loop for subscription sync.
     */
    //    @Scheduled(fixedDelayString = "${application.noosphere.chain.snapshot-sync.sync-period}")
    public void subscriptionSyncLoop() {
        if (isSnapshotSyncing.get()) {
            return; // Don't run if shutting down or initial sync is in progress
        }

        try {
            long headBlock = web3j.ethBlockNumber().send().getBlockNumber().longValue() - properties.getTrailHeadBlocks();
            long currentLastBlock = lastSyncedBlock.get();

            if (currentLastBlock < headBlock) {
                long targetBlock = Math.min(headBlock, currentLastBlock + 100); // Sync max 100 blocks
                log.info("New blocks detected. Syncing from {} to {}", currentLastBlock + 1, targetBlock);

                snapshotSync(targetBlock).join(); // Wait for sync to complete

                lastSyncedBlock.set(targetBlock);
                long newHeadSubId = web3Router.getLastSubscriptionId(headBlock).join().longValue();
                lastSubscriptionId.set(newHeadSubId);

                log.info("Sync complete. Last synced block: {}, Last sub ID: {}", targetBlock, newHeadSubId);
            } else {
                log.debug("No new blocks to sync. Current head: {}", headBlock);
            }
        } catch (Exception e) {
            log.error("Error during periodic sync loop", e);
        }
    }

    /**
     * Starts listening for 'RequestStarted' events using a reactive Flowable.
     */
    private void startListeningForEvents() {
        log.info("Attempting to subscribe to RequestStarted events...");
        Router routerContract = web3Router.getRouterContract();

        // Dispose of the old subscription if it exists to prevent duplicates
        if (this.requestStartedSubscription != null && !this.requestStartedSubscription.isDisposed()) {
            this.requestStartedSubscription.dispose();
        }

        this.requestStartedSubscription = routerContract
            .requestStartEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
            .subscribe(
                this::processRequestStartedEvent, // onNext: when a new event arrives
                error -> { // onError
                    log.error("Error in RequestStarted event subscription. It might be closed. Re-subscribing in 10 seconds...", error);
                    // Schedule a reconnection attempt after a delay
                    CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(this::startListeningForEvents);
                },
                () -> { // onComplete
                    log.info("RequestStarted event subscription stream completed unexpectedly. Re-subscribing in 10 seconds...");
                    // Also attempt to reconnect if the stream completes, as it shouldn't for a live listener
                    CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(this::startListeningForEvents);
                }
            );
    }

    /**
     * Processes a single 'RequestStarted' event.
     */
    private void processRequestStartedEvent(Router.RequestStartEventResponse event) {
        log.info(
            "Processing RequestStarted event: requestId={}, subscriptionId={}, containerId={}",
            Numeric.toHexString(event.requestId),
            event.subscriptionId,
            Numeric.toHexString(event.containerId)
        );

        // Asynchronously fetch subscription details to get the wallet address
        web3Router
            .getComputeSubscription(event.subscriptionId)
            .thenAccept(computeSubscription -> {
                if (computeSubscription == null) {
                    log.error("Could not find subscription details for ID: {}", event.subscriptionId);
                    return;
                }

                // Create the DTO using the static factory method, which also creates the Commitment
                OnchainRequestDTO requestDTO = OnchainRequestDTO.fromEvent(event, computeSubscription.wallet);
                requestDTO.setSubscription(
                    subscriptionMapper.toDto(event.subscriptionId.longValue(), computeSubscription, containerLookupService)
                );
                requestDTO.setRequiresProof(Optional.of(computeSubscription.verifier != null && !computeSubscription.verifier.isEmpty()));

                boolean isValid = requestValidatorService.validateOnChainRequest(requestDTO);
                if (isValid) {
                    blockChainService.processIncomingRequest(requestDTO);
                    log.info("Relayed on-chain request to computation service: requestId={}", Numeric.toHexString(event.requestId));
                } else {
                    log.warn("Ignored invalid on-chain request: requestId={}", Numeric.toHexString(event.requestId));
                }
            })
            .exceptionally(ex -> {
                log.error("Failed to process RequestStarted event for subscriptionId {}", event.subscriptionId, ex);
                return null;
            });
    }

    /**
     * Snapshot syncs subscriptions from the Coordinator up to the given head block.
     */
    private CompletableFuture<Void> snapshotSync(long headBlock) {
        return CompletableFuture.runAsync(() -> {
            try {
                long headSubId = web3Router.getLastSubscriptionId(headBlock).join().longValue();
                log.info("Snapshot sync: Found highest subscription ID {} at block {}", headSubId, headBlock);

                long startId = lastSubscriptionId.get() + 1;
                if (startId > headSubId) {
                    log.info("No new subscriptions to sync.");
                    return;
                }

                List<int[]> batches = getBatches((int) startId, (int) headSubId, properties.getSnapshotSync().getBatchSize());
                log.info("Syncing new subscriptions in {} batches.", batches.size());

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int[] batch : batches) {
                    futures.add(syncSubscriptionBatchWithRetry(batch[0], batch[1], headBlock));
                    // Sleep between launching batches to avoid overwhelming the RPC
                    try {
                        Thread.sleep(properties.getSnapshotSync().getSleep());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Snapshot sync sleep interrupted.");
                    }
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } catch (Exception e) {
                log.error("Failed to complete snapshot sync", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Async
    public CompletableFuture<Void> syncSubscriptionBatchWithRetry(int startId, int endId, long blockNumber) {
        // Simple retry mechanism. For production, consider Spring Retry or a more robust library.
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                syncBatchSubscriptionsCreation(startId, endId, blockNumber).join();
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                log.error("Error syncing subscription batch [{}-{}], attempt {}/{}. Retrying...", startId, endId, attempt, maxRetries, e);
                if (attempt == maxRetries) {
                    throw new RuntimeException("Failed to sync subscription batch after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(properties.getSnapshotSync().getSleep() * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry sleep interrupted", ie);
                }
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> updateResponseCountsForLastInterval(List<SubscriptionDTO> subscriptions, long blockNumber) {
        List<SubscriptionDTO> subsOnLastInterval = subscriptions.stream().filter(SubscriptionDTO::isOnLastInterval).toList();

        if (subsOnLastInterval.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<Long> filteredIds = subsOnLastInterval.stream().map(SubscriptionDTO::getId).toList();
        List<Long> filteredIntervals = subsOnLastInterval.stream().map(SubscriptionDTO::getInterval).toList();

        return web3BatchReader
            .getIntervalStatuses(filteredIds, filteredIntervals, blockNumber)
            .thenAccept(responseCounts -> {
                Assert.isTrue(filteredIds.size() == responseCounts.size(), "Mismatched sizes for ids and counts");
                for (int i = 0; i < filteredIds.size(); i++) {
                    long subId = filteredIds.get(i);
                    long interval = filteredIntervals.get(i);
                    long count = responseCounts.get(i).redundancyCount.longValue();

                    // Find the corresponding subscription and update its state
                    subsOnLastInterval
                        .stream()
                        .filter(s -> s.getId() == subId)
                        .findFirst()
                        .ifPresent(s -> s.setResponseCount(interval, count));
                }
            });
    }

    private CompletableFuture<Void> syncBatchSubscriptionsCreation(int startId, int endId, long blockNumber) {
        return CompletableFuture.runAsync(() -> {
            // Fetch subscriptions from the batch reader
            List<SubscriptionBatchReader.ComputeSubscription> contractSubscriptions = web3BatchReader
                .getSubscriptions(startId, endId, blockNumber)
                .join();

            // Convert contract DTOs to internal Subscription objects
            List<SubscriptionDTO> subscriptions = new ArrayList<>();
            for (int i = 0; i < contractSubscriptions.size(); i++) {
                subscriptions.add(subscriptionMapper.toDto((long) startId + i, contractSubscriptions.get(i), containerLookupService));
            }

            updateResponseCountsForLastInterval(subscriptions, blockNumber).join();

            // Process each subscription
            for (SubscriptionDTO subscription : subscriptions) {
                OnchainRequestDTO requestDTO = OnchainRequestDTO.builder()
                    .subscription(subscription)
                    .requiresProof(Optional.of(subscription.getVerifier() != null && !subscription.getVerifier().isEmpty()))
                    .build();
                boolean isValid = requestValidatorService.validateOnChainRequest(requestDTO);

                if (isValid) {
                    blockChainService.processIncomingRequest(requestDTO);
                    log.info("Relayed subscription creation: id={}", subscription.getId());
                } else {
                    log.warn("Ignored subscription creation: id={}", subscription.getId());
                }
            }
        });
    }

    private List<int[]> getBatches(int start, int end, int batchSize) {
        List<int[]> batches = new ArrayList<>();
        if (start > end) {
            return batches;
        }
        for (int i = start; i <= end; i += batchSize) {
            batches.add(new int[] { i, Math.min(i + batchSize - 1, end) });
        }
        return batches;
    }
}

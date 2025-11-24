package io.hpp.noosphere.agent.service.blockchain;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.contracts.SubscriptionBatchReader;
import io.hpp.noosphere.agent.service.ContainerLookupService;
import io.hpp.noosphere.agent.service.NoosphereConfigService;
import io.hpp.noosphere.agent.service.RequestValidatorService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3RouterService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3SubscriptionBatchReaderService;
import io.hpp.noosphere.agent.service.dto.OnchainRequestDTO;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

    private final AtomicLong lastSyncedBlock = new AtomicLong(0);
    private final AtomicLong lastSubscriptionId = new AtomicLong(0);
    private final AtomicBoolean isSnapshotSyncing = new AtomicBoolean(false);

    public BlockchainListener(
        Web3j web3j,
        Web3RouterService web3Router,
        Web3SubscriptionBatchReaderService web3BatchReader,
        RequestValidatorService requestValidatorService,
        BlockChainService blockChainService,
        NoosphereConfigService noosphereConfigService,
        ContainerLookupService containerLookupService
    ) {
        this.web3j = web3j;
        this.web3Router = web3Router;
        this.web3BatchReader = web3BatchReader;
        this.requestValidatorService = requestValidatorService;
        this.blockChainService = blockChainService;
        this.properties = noosphereConfigService.getActiveConfig().getChain();
        this.containerLookupService = containerLookupService;
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
            isSnapshotSyncing.set(true);
            snapshotSync(headBlock).join(); // Wait for snapshot sync to complete
            isSnapshotSyncing.set(false);

            long headSubId = web3Router.getLastSubscriptionId(headBlock).join().longValue(); // This now returns the highest ID directly
            lastSubscriptionId.set(headSubId);

            log.info("Finished snapshot sync. Last synced block: {}, Last sub ID: {}", headBlock, headSubId);
        } catch (Exception e) {
            log.error("Fatal error during initial snapshot sync. Shutting down.", e);
            // In a real app, you might want to shut down the application context here.
        }
    }

    /**
     * Core processing loop.
     */
    @Scheduled(fixedDelayString = "${application.noosphere.chain.snapshot-sync.sync-period}")
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
                long newHeadSubId = web3Router.getLastSubscriptionId(headBlock).join().longValue(); // This now returns the highest ID directly
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
     * Snapshot syncs subscriptions from the Coordinator up to the given head block.
     */
    private CompletableFuture<Void> snapshotSync(long headBlock) {
        return CompletableFuture.runAsync(() -> {
            try {
                long headSubId = web3Router.getLastSubscriptionId(headBlock).join().longValue(); // This now returns the highest ID directly
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
                long currentId = (long) startId + i;
                subscriptions.add(
                    SubscriptionDTO.builder()
                        .id(currentId)
                        .routeId(new String(contractSubscriptions.get(i).routeId, StandardCharsets.UTF_8).replaceAll("\\x00+$", ""))
                        .containerId(containerLookupService.getContainers(contractSubscriptions.get(i).containerId).getFirst())
                        .feeAmount(contractSubscriptions.get(i).feeAmount)
                        .feeToken(contractSubscriptions.get(i).feeToken)
                        .client(contractSubscriptions.get(i).client)
                        .activeAt(contractSubscriptions.get(i).activeAt.longValue())
                        .intervalSeconds(contractSubscriptions.get(i).intervalSeconds.longValue())
                        .maxExecutions(contractSubscriptions.get(i).maxExecutions.longValue())
                        .wallet(contractSubscriptions.get(i).wallet)
                        .verifier(contractSubscriptions.get(i).verifier)
                        .redundancy(contractSubscriptions.get(i).redundancy.intValue())
                        .useDeliveryInbox(contractSubscriptions.get(i).useDeliveryInbox)
                        .build()
                );
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

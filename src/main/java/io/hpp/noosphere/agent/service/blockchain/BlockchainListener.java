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
import java.util.ArrayList;
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
            .requestStartEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
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
}

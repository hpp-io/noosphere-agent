package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.Coordinator;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinatorService {

    private final Web3RouterService web3RouterService;
    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    private Coordinator coordinatorContract;

    /**
     * Load the Coordinator contract address through Web3RouterService during service initialization.
     */
    @PostConstruct
    public void init() {
        try {
            // 1. Query the address of the "Coordinator" contract asynchronously through the Router service.
            String coordinatorAddress = web3RouterService.getCachedContractAddress("Coordinator_v1.0.0");

            // 2. Load the Coordinator contract's Web3j wrapper using the retrieved address.
            this.coordinatorContract = Coordinator.load(coordinatorAddress, web3j, credentials, gasProvider);

            log.info("Successfully initialized CoordinatorService with contract at address: {}", coordinatorAddress);
        } catch (Exception e) {
            log.error("Failed to initialize CoordinatorService", e);
            // You can stop application execution or add retry logic here.
            throw new RuntimeException("Could not initialize CoordinatorService", e);
        }
    }

    /**
     * Example function to query configuration information from the Coordinator contract.
     *
     * @return CompletableFuture containing BillingConfig information
     */
    public CompletableFuture<Coordinator.BillingConfig> getConfig() {
        if (coordinatorContract == null) {
            log.error("Coordinator contract is not loaded.");
            return CompletableFuture.failedFuture(new IllegalStateException("Coordinator contract not initialized."));
        }

        log.debug("Fetching config from Coordinator contract");
        // 3. Call the smart contract function using the loaded contract object.
        return coordinatorContract.getConfig().sendAsync();
    }
}

package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.DelegateeCoordinator;
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
public class DelegateeCoordinatorService {

    private final Web3RouterService web3RouterService;
    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    private DelegateeCoordinator delegateeCoordinatorContract;

    /**
     * Load the DelegateeCoordinator contract address through Web3RouterService during service initialization.
     */
    @PostConstruct
    public void init() {
        try {
            // 1. Query the address of the "DelegateeCoordinator" contract through the Router service.
            String contractAddress = web3RouterService.getContractAddress("DelegateeCoordinator").join();

            if (
                contractAddress == null || contractAddress.isEmpty() || contractAddress.equals("0x0000000000000000000000000000000000000000")
            ) {
                throw new IllegalStateException("DelegateeCoordinator contract address not found via Router.");
            }

            // 2. Load the DelegateeCoordinator contract's Web3j wrapper using the retrieved address.
            this.delegateeCoordinatorContract = DelegateeCoordinator.load(contractAddress, web3j, credentials, gasProvider);

            log.info("Successfully initialized DelegateeCoordinatorService with contract at address: {}", contractAddress);
        } catch (Exception e) {
            log.error("Failed to initialize DelegateeCoordinatorService", e);
            throw new RuntimeException("Could not initialize DelegateeCoordinatorService", e);
        }
    }

    /**
     * Example function to query configuration information from the DelegateeCoordinator contract.
     * @return CompletableFuture containing BillingConfig information
     */
    public CompletableFuture<DelegateeCoordinator.BillingConfig> getConfig() {
        if (delegateeCoordinatorContract == null) {
            log.error("DelegateeCoordinator contract is not loaded.");
            return CompletableFuture.failedFuture(new IllegalStateException("DelegateeCoordinator contract not initialized."));
        }

        log.debug("Fetching config from DelegateeCoordinator contract");
        // 3. Call the smart contract function using the loaded contract object.
        return delegateeCoordinatorContract.getConfig().sendAsync();
    }
}

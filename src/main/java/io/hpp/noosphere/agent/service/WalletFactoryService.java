package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.WalletFactory;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletFactoryService {

    private final Web3RouterService web3RouterService;
    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    private WalletFactory walletFactoryContract;

    /**
     * Load the WalletFactory contract address through Web3RouterService during service initialization.
     */
    @PostConstruct
    public void init() {
        try {
            // 1. Query the address of the "WalletFactory" contract through the Router service.
            String contractAddress = web3RouterService.getCachedContractAddress("WalletFactory");

            if (
                contractAddress == null || contractAddress.isEmpty() || contractAddress.equals("0x0000000000000000000000000000000000000000")
            ) {
                throw new IllegalStateException("WalletFactory contract address not found via Router.");
            }

            // 2. Load the WalletFactory contract's Web3j wrapper using the retrieved address.
            this.walletFactoryContract = WalletFactory.load(contractAddress, web3j, credentials, gasProvider);

            log.info("Successfully initialized WalletFactoryService with contract at address: {}", contractAddress);
        } catch (Exception e) {
            log.error("Failed to initialize WalletFactoryService", e);
            throw new RuntimeException("Could not initialize WalletFactoryService", e);
        }
    }

    /**
     * Example function to create a new Wallet.
     * @param initialOwner The initial owner address for the new Wallet
     * @return CompletableFuture containing the transaction receipt
     */
    public CompletableFuture<TransactionReceipt> createWallet(String initialOwner) {
        if (walletFactoryContract == null) {
            log.error("WalletFactory contract is not loaded.");
            return CompletableFuture.failedFuture(new IllegalStateException("WalletFactory contract not initialized."));
        }

        log.info("Creating a new wallet for owner: {}", initialOwner);
        // 3. Call the smart contract function using the loaded contract object.
        return walletFactoryContract.createWallet(initialOwner).sendAsync();
    }

    public Boolean isValid(String walletAddress) throws Exception {
        if (walletFactoryContract == null) {
            log.error("WalletFactory contract is not loaded.");
            throw new IllegalStateException("WalletFactory contract not initialized.");
        }

        log.info("Creating a new wallet for owner: {}", walletAddress);
        // 3. Call the smart contract function using the loaded contract object.
        try {
            return walletFactoryContract.isValidWallet(walletAddress).send();
        } catch (Exception e) {
            log.error("Failed to is valid wallet: {}", walletAddress, e);
            throw new RuntimeException("Failed to is valid wallet", e);
        }
    }
}

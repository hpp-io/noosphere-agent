package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.Router;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

@Slf4j
@Service
@RequiredArgsConstructor
public class Web3RouterService {

    private final NoosphereConfigService noosphereConfigService;
    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    private Router routerContract;
    private final Map<String, String> contractAddresses = new HashMap<>();

    @PostConstruct
    public void init() {
        ApplicationProperties.Chain chainConfig = noosphereConfigService.getActiveConfig().getChain();
        String routerAddress = chainConfig.getRouterAddress();

        if (routerAddress == null || routerAddress.isEmpty()) {
            throw new IllegalStateException("Router contract configuration not found in noosphere config");
        }

        this.routerContract = Router.load(routerAddress, web3j, credentials, gasProvider);

        log.info("Initialized Router contract at address: {}", routerAddress);

        // Cache contract addresses during initialization
        loadContractAddresses();
    }

    /**
     * Get the address of a specific contract from the Router
     */
    public CompletableFuture<String> getContractAddress(String contractName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                if (contractAddresses.containsKey(contractName)) {
                    return contractAddresses.get(contractName);
                }

                // Query address from Router contract
                // Convert string to 32-byte array (pad right with zeros)
                byte[] contractNameBytes = contractName.getBytes();
                byte[] paddedBytes = new byte[32];
                System.arraycopy(contractNameBytes, 0, paddedBytes, 0, Math.min(contractNameBytes.length, 32));

                String address = routerContract.getContractById(paddedBytes).send();

                // Store in cache
                contractAddresses.put(contractName, address);

                log.info("Retrieved contract address for {}: {}", contractName, address);
                return address;
            } catch (Exception e) {
                log.error("Failed to get contract address for {}", contractName, e);
                throw new RuntimeException("Failed to get contract address", e);
            }
        });
    }

    /**
     * Get addresses of multiple contracts at once
     */
    public CompletableFuture<Map<String, String>> getMultipleContractAddresses(String... contractNames) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> addresses = new HashMap<>();

            for (String contractName : contractNames) {
                try {
                    String address = getContractAddress(contractName).join();
                    addresses.put(contractName, address);
                } catch (Exception e) {
                    log.error("Failed to get address for contract: {}", contractName, e);
                    addresses.put(contractName, null);
                }
            }

            return addresses;
        });
    }

    /**
     * Get addresses of contracts predefined in NoosphereConfig
     */
    public CompletableFuture<Map<String, String>> getConfiguredContractAddresses() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> addresses = new HashMap<>();
                ApplicationProperties.Chain chainConfig = noosphereConfigService.getActiveConfig().getChain();

                // Query dynamically registered contracts through Router
                String[] knownContracts = getKnownContractNames();
                for (String contractName : knownContracts) {
                    if (!addresses.containsKey(contractName)) {
                        try {
                            // Convert string to 32-byte array
                            byte[] contractNameBytes = contractName.getBytes();
                            byte[] paddedBytes = new byte[32];
                            System.arraycopy(contractNameBytes, 0, paddedBytes, 0, Math.min(contractNameBytes.length, 32));

                            String address = routerContract.getContractById(paddedBytes).send();
                            if (address != null && !address.equals("0x0000000000000000000000000000000000000000")) {
                                addresses.put(contractName, address);
                                contractAddresses.put(contractName, address); // Also store in cache
                            }
                        } catch (Exception e) {
                            log.debug("Contract {} not found in router", contractName);
                        }
                    }
                }

                log.info("Retrieved {} contract addresses", addresses.size());
                return addresses;
            } catch (Exception e) {
                log.error("Failed to get configured contract addresses", e);
                throw new RuntimeException("Failed to get configured contract addresses", e);
            }
        });
    }

    /**
     * Check if a contract address is registered
     */
    public CompletableFuture<Boolean> isContractRegistered(String contractName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Convert string to 32-byte array
                byte[] contractNameBytes = contractName.getBytes();
                byte[] paddedBytes = new byte[32];
                System.arraycopy(contractNameBytes, 0, paddedBytes, 0, Math.min(contractNameBytes.length, 32));

                String address = routerContract.getContractById(paddedBytes).send();
                return address != null && !address.equals("0x0000000000000000000000000000000000000000");
            } catch (Exception e) {
                log.error("Failed to check if contract {} is registered", contractName, e);
                return false;
            }
        });
    }

    /**
     * Return cached address (synchronous)
     */
    public String getCachedContractAddress(String contractName) {
        return contractAddresses.get(contractName);
    }

    /**
     * Return all cached addresses
     */
    public Map<String, String> getAllCachedAddresses() {
        return new HashMap<>(contractAddresses);
    }

    /**
     * Load contract addresses during initialization
     */
    private void loadContractAddresses() {
        CompletableFuture.runAsync(() -> {
            try {
                // First load statically defined contracts from configuration file
                ApplicationProperties.Chain chainConfig = noosphereConfigService.getActiveConfig().getChain();

                if (chainConfig.getRouterAddress() != null && !chainConfig.getRouterAddress().isEmpty()) {
                    contractAddresses.put("Router", chainConfig.getRouterAddress());
                }

                // Load dynamic contracts through Router
                String[] knownContracts = getKnownContractNames();

                for (String contractName : knownContracts) {
                    if (!contractAddresses.containsKey(contractName)) {
                        try {
                            // Convert string to 32-byte array
                            byte[] contractNameBytes = contractName.getBytes();
                            byte[] paddedBytes = new byte[32];
                            System.arraycopy(contractNameBytes, 0, paddedBytes, 0, Math.min(contractNameBytes.length, 32));

                            String address = routerContract.getContractById(paddedBytes).send();
                            if (address != null && !address.equals("0x0000000000000000000000000000000000000000")) {
                                contractAddresses.put(contractName, address);
                                log.debug("Cached address for {}: {}", contractName, address);
                            }
                        } catch (Exception e) {
                            log.debug("Contract {} not found in router", contractName);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to load contract addresses during initialization", e);
            }
        });
    }

    /**
     * Return known contract names
     * This list can be extended to be configurable from NoosphereConfig or external configuration
     */
    private String[] getKnownContractNames() {
        // This part can be extended to be configurable from NoosphereConfig
        return new String[] { "Coordinator_v1.0.0", "DelegateeCoordinator", "Router", "Wallet", "WalletFactory" };
    }

    /**
     * Force refresh cache
     */
    public CompletableFuture<Void> refreshCache() {
        return CompletableFuture.runAsync(() -> {
            contractAddresses.clear();
            loadContractAddresses();
        });
    }
}

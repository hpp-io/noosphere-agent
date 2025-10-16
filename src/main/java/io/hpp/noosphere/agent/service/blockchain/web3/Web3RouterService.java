package io.hpp.noosphere.agent.service.blockchain.web3;

import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.Router;
import io.hpp.noosphere.agent.service.NoosphereConfigService;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;

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
    public Map<String, String> getConfiguredContractAddresses() {
        // Query dynamically registered contracts through Router
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
                        contractAddresses.put(contractName, address); // Also store in cache
                    }
                } catch (Exception e) {
                    log.debug("Contract {} not found in router", contractName);
                }
            }
        }

        //Query wallet factory address
        try {
            String walletFactoryAddress = routerContract.getWalletFactory().send();
            if (walletFactoryAddress != null && !walletFactoryAddress.equals("0x0000000000000000000000000000000000000000")) {
                contractAddresses.put("WalletFactory", walletFactoryAddress);
            }
        } catch (Exception e) {
            log.debug("Contract WalletFactory not found in router");
        }

        log.info("Retrieved {} contract addresses", contractAddresses.size());
        return contractAddresses;
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
        try {
            // First load statically defined contracts from configuration file
            ApplicationProperties.Chain chainConfig = noosphereConfigService.getActiveConfig().getChain();

            if (chainConfig.getRouterAddress() != null && !chainConfig.getRouterAddress().isEmpty()) {
                contractAddresses.put("Router", chainConfig.getRouterAddress());
            }
            this.getConfiguredContractAddresses();
        } catch (Exception e) {
            log.error("Failed to load contract addresses during initialization", e);
        }
    }

    /**
     * Return known contract names
     * This list can be extended to be configurable from NoosphereConfig or external configuration
     */
    private String[] getKnownContractNames() {
        // This part can be extended to be configurable from NoosphereConfig
        return new String[] { "Coordinator_v1.0.0" };
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

    /**
     * Get the last (highest) subscription ID from the Router contract.
     * @param blockNumber Optional block number to query from. If null, queries the latest block.
     * @return A CompletableFuture containing the last subscription ID.
     */
    public CompletableFuture<BigInteger> getLastSubscriptionId(Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                    Router.FUNC_GETLASTSUBSCRIPTIONID,
                    java.util.Collections.emptyList(),
                    java.util.Collections.singletonList(new TypeReference<Uint64>() {})
                );
                String encodedFunction = FunctionEncoder.encode(function);

                DefaultBlockParameter blockParameter;
                if (blockNumber != null && blockNumber > 0) {
                    log.debug("Querying last subscription ID from router contract at block {}", blockNumber);
                    blockParameter = DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber));
                } else {
                    log.debug("Querying last subscription ID from router contract at latest block...");
                    blockParameter = DefaultBlockParameter.valueOf("latest");
                }

                org.web3j.protocol.core.methods.request.Transaction transaction =
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        credentials.getAddress(),
                        routerContract.getContractAddress(),
                        encodedFunction
                    );

                String result = web3j.ethCall(transaction, blockParameter).send().getValue();
                BigInteger lastId = (BigInteger) FunctionReturnDecoder.decode(result, function.getOutputParameters()).get(0).getValue();

                log.info("Retrieved last subscription ID: {} at block {}", lastId, blockNumber != null ? blockNumber : "latest");
                return lastId;
            } catch (Exception e) {
                log.error("Failed to get last subscription ID", e);
                throw new RuntimeException("Failed to get last subscription ID", e);
            }
        });
    }

    /**
     * Get the last (highest) subscription ID from the Router contract at the latest block.
     * @return A CompletableFuture containing the last subscription ID.
     */
    public CompletableFuture<BigInteger> getLastSubscriptionId() {
        return getLastSubscriptionId(null);
    }

    /**
     * Get a specific compute subscription by its ID.
     * @param subscriptionId The ID of the subscription.
     * @return A CompletableFuture containing the subscription details.
     */
    public CompletableFuture<Router.ComputeSubscription> getComputeSubscription(BigInteger subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Querying subscription details for ID: {}", subscriptionId);
                Router.ComputeSubscription subscription = routerContract.getComputeSubscription(subscriptionId).send();
                if (subscription == null) {
                    log.warn("No subscription found for ID: {}", subscriptionId);
                    return null;
                }
                log.info("Retrieved subscription details for ID: {}", subscriptionId);
                return subscription;
            } catch (Exception e) {
                log.error("Failed to get compute subscription for ID {}", subscriptionId, e);
                throw new RuntimeException("Failed to get compute subscription", e);
            }
        });
    }
}

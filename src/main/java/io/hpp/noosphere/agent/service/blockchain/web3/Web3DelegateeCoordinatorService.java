package io.hpp.noosphere.agent.service.blockchain.web3;

import static io.hpp.noosphere.agent.config.Constants.ZERO_ADDRESS;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.DelegateeCoordinator;
import io.hpp.noosphere.agent.contracts.Router;
import io.hpp.noosphere.agent.service.blockchain.dto.SignatureParamsDTO;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple8;

@Service
public class Web3DelegateeCoordinatorService {

    private static final Logger log = LoggerFactory.getLogger(Web3DelegateeCoordinatorService.class);

    private final Web3RouterService web3RouterService;
    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    private DelegateeCoordinator delegateeCoordinatorContract;

    public Web3DelegateeCoordinatorService(
        Web3RouterService web3RouterService,
        Web3j web3j,
        Credentials credentials,
        Web3jConfig.CustomGasProvider gasProvider
    ) {
        this.web3RouterService = web3RouterService;
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /**
     * Load the DelegateeCoordinator contract address through Web3RouterService during service initialization.
     */
    @PostConstruct
    public void init() {
        try {
            // 1. Query the address of the "DelegateeCoordinator" contract through the Router service.
            String contractAddress = web3RouterService.getCachedContractAddress("Coordinator_v1.0.0");

            if (contractAddress == null || contractAddress.isEmpty() || contractAddress.equals(ZERO_ADDRESS)) {
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
     *
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

    /**
     * Get a specific compute subscription by its ID from the Router.
     * Note: Subscription data is stored in the Router, not the Coordinator.
     *
     * @param subscriptionId The ID of the subscription.
     * @return A CompletableFuture containing the subscription details.
     */
    public CompletableFuture<Router.ComputeSubscription> getSubscription(BigInteger subscriptionId) {
        if (delegateeCoordinatorContract == null) {
            log.error("DelegateeCoordinator contract is not loaded.");
            return CompletableFuture.failedFuture(new IllegalStateException("DelegateeCoordinator contract not initialized."));
        }
        // The actual subscription data is fetched from the Router contract
        return web3RouterService.getComputeSubscription(subscriptionId);
    }

    /**
     * Get the protocol fee from the DelegateeCoordinator contract.
     *
     * @return A CompletableFuture containing the protocol fee.
     */
    public CompletableFuture<BigInteger> getProtocolFee() {
        if (delegateeCoordinatorContract == null) {
            log.error("DelegateeCoordinator contract is not loaded.");
            return CompletableFuture.failedFuture(new IllegalStateException("DelegateeCoordinator contract not initialized."));
        }

        log.debug("Fetching protocol fee from DelegateeCoordinator contract");
        return delegateeCoordinatorContract.getProtocolFee().sendAsync();
    }

    public CompletableFuture<TransactionReceipt> acceptOwnership() {
        return checkContractLoaded().thenCompose(c -> c.acceptOwnership().sendAsync());
    }

    public CompletableFuture<TransactionReceipt> cancelRequest(byte[] requestId) {
        return checkContractLoaded().thenCompose(c -> c.cancelRequest(requestId).sendAsync());
    }

    public CompletableFuture<TransactionReceipt> initialize(DelegateeCoordinator.BillingConfig config) {
        return checkContractLoaded().thenCompose(c -> c.initialize(config).sendAsync());
    }

    public CompletableFuture<TransactionReceipt> prepareNextInterval(
        BigInteger subscriptionId,
        BigInteger nextInterval,
        String nodeWallet
    ) {
        return checkContractLoaded().thenCompose(c -> c.prepareNextInterval(subscriptionId, nextInterval, nodeWallet).sendAsync());
    }

    public CompletableFuture<TransactionReceipt> reportComputeResult(
        BigInteger deliveryInterval,
        byte[] input,
        byte[] output,
        byte[] proof,
        byte[] commitmentData,
        String agentWallet
    ) {
        return checkContractLoaded()
            .thenCompose(c -> c.reportComputeResult(deliveryInterval, input, output, proof, commitmentData, agentWallet).sendAsync());
    }

    /**
     * Simulates a reportComputeResult transaction using eth_call.
     */
    public CompletableFuture<Void> simulateReportComputeResult(
        BigInteger deliveryInterval,
        byte[] input,
        byte[] output,
        byte[] proof,
        byte[] commitmentData,
        String agentWallet
    ) {
        return checkContractLoaded()
            .thenCompose(contract ->
                CompletableFuture.runAsync(() -> {
                    try {
                        String encodedFunction = contract
                            .reportComputeResult(deliveryInterval, input, output, proof, commitmentData, agentWallet)
                            .encodeFunctionCall();

                        org.web3j.protocol.core.methods.request.Transaction transaction =
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                                credentials.getAddress(),
                                contract.getContractAddress(),
                                encodedFunction
                            );

                        web3j.ethCall(transaction, DefaultBlockParameter.valueOf("latest")).send();
                    } catch (Exception e) {
                        log.error("Simulation of reportComputeResult failed", e);
                        throw new RuntimeException("Simulation failed", e);
                    }
                })
            );
    }

    /**
     * Encodes the transaction data for a reportComputeResult call.
     *
     * @return The encoded function call as a hex string.
     */
    public String getReportComputeResultTxData(
        BigInteger deliveryInterval,
        byte[] input,
        byte[] output,
        byte[] proof,
        byte[] commitmentData,
        String agentWallet
    ) {
        if (delegateeCoordinatorContract == null) {
            throw new IllegalStateException("DelegateeCoordinator contract not initialized.");
        }
        return delegateeCoordinatorContract
            .reportComputeResult(deliveryInterval, input, output, proof, commitmentData, agentWallet)
            .encodeFunctionCall();
    }

    public CompletableFuture<TransactionReceipt> reportDelegatedComputeResult(
        BigInteger nonce,
        BigInteger expiry,
        DelegateeCoordinator.ComputeSubscription sub,
        byte[] signature,
        BigInteger deliveryInterval,
        byte[] input,
        byte[] output,
        byte[] proof,
        String nodeWallet
    ) {
        return checkContractLoaded()
            .thenCompose(c ->
                c
                    .reportDelegatedComputeResult(nonce, expiry, sub, signature, deliveryInterval, input, output, proof, nodeWallet)
                    .sendAsync()
            );
    }

    /**
     * Simulates a reportDelegatedComputeResult transaction using eth_call.
     */
    public CompletableFuture<Void> simulateReportDelegatedComputeResult(
        SubscriptionDTO subscription,
        SignatureParamsDTO signature,
        BigInteger deliveryInterval,
        byte[] input,
        byte[] output,
        byte[] proof,
        String nodeWallet
    ) {
        // Convert SubscriptionDTO to the contract's struct type
        DelegateeCoordinator.ComputeSubscription subStruct = subscription.toCoordinatorComputeSubscription();

        return checkContractLoaded()
            .thenCompose(contract ->
                CompletableFuture.runAsync(() -> {
                    try {
                        String encodedFunction = contract
                            .reportDelegatedComputeResult(
                                BigInteger.valueOf(signature.nonce()),
                                BigInteger.valueOf(signature.expiry()),
                                subStruct,
                                signature.signature(),
                                deliveryInterval,
                                input,
                                output,
                                proof,
                                nodeWallet
                            )
                            .encodeFunctionCall();

                        org.web3j.protocol.core.methods.request.Transaction transaction =
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                                credentials.getAddress(),
                                contract.getContractAddress(),
                                encodedFunction
                            );

                        // We send the eth_call and check for errors. A successful call returns a value,
                        // but a revert will throw an exception.
                        web3j.ethCall(transaction, DefaultBlockParameter.valueOf("latest")).send();
                    } catch (Exception e) {
                        log.error("Simulation of reportDelegatedComputeResult failed", e);
                        // Propagate the exception to the CompletableFuture chain
                        throw new RuntimeException("Simulation failed", e);
                    }
                })
            );
    }

    /**
     * Encodes the transaction data for a reportDelegatedComputeResult call.
     */
    public String getReportDelegatedComputeResultTxData(
        SubscriptionDTO subscription,
        SignatureParamsDTO signature,
        BigInteger deliveryInterval,
        byte[] input,
        byte[] output,
        byte[] proof,
        String nodeWallet
    ) {
        DelegateeCoordinator.ComputeSubscription subStruct = subscription.toCoordinatorComputeSubscription();
        return checkContractLoaded()
            .join()
            .reportDelegatedComputeResult(
                BigInteger.valueOf(signature.nonce()),
                BigInteger.valueOf(signature.expiry()),
                subStruct,
                signature.signature(),
                deliveryInterval,
                input,
                output,
                proof,
                nodeWallet
            )
            .encodeFunctionCall();
    }

    public CompletableFuture<TransactionReceipt> setSubscriptionBatchReader(String reader) {
        return checkContractLoaded().thenCompose(c -> c.setSubscriptionBatchReader(reader).sendAsync());
    }

    public CompletableFuture<TransactionReceipt> startRequest(
        byte[] requestId,
        BigInteger subscriptionId,
        byte[] containerId,
        BigInteger interval,
        BigInteger redundancy,
        Boolean useDeliveryInbox,
        String feeToken,
        BigInteger feeAmount,
        String wallet,
        String verifier
    ) {
        return checkContractLoaded()
            .thenCompose(c ->
                c
                    .startRequest(
                        requestId,
                        subscriptionId,
                        containerId,
                        interval,
                        redundancy,
                        useDeliveryInbox,
                        feeToken,
                        feeAmount,
                        wallet,
                        verifier
                    )
                    .sendAsync()
            );
    }

    public CompletableFuture<TransactionReceipt> transferOwnership(String to) {
        return checkContractLoaded().thenCompose(c -> c.transferOwnership(to).sendAsync());
    }

    public CompletableFuture<TransactionReceipt> updateConfig(DelegateeCoordinator.BillingConfig config) {
        return checkContractLoaded().thenCompose(c -> c.updateConfig(config).sendAsync());
    }

    /**
     * Checks if a node has already delivered a response for a given interval at a specific block.
     *
     * @param subscriptionId The ID of the subscription.
     * @param interval       The interval number.
     * @param nodeAddress    The address of the node.
     * @param blockNumber    The block number to query at. If null, queries the latest block.
     * @return A CompletableFuture containing true if the node has responded, false otherwise.
     */
    public CompletableFuture<Boolean> getNodeHasDeliveredResponse(
        long subscriptionId,
        long interval,
        String nodeAddress,
        Long blockNumber
    ) {
        return checkContractLoaded()
            .thenCompose(contract ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // The key for the nodeResponded mapping is keccak256(abi.encode(subscriptionId, interval, nodeAddress))
                        final org.web3j.abi.datatypes.Function dummyFunction = new org.web3j.abi.datatypes.Function(
                            "dummy",
                            Arrays.asList(
                                new Uint(BigInteger.valueOf(subscriptionId)),
                                new Uint(BigInteger.valueOf(interval)),
                                new Address(nodeAddress)
                            ),
                            java.util.Collections.emptyList()
                        );
                        String encodedParameters = FunctionEncoder.encode(dummyFunction).substring(10);
                        byte[] hashedKey = Hash.sha3(org.web3j.utils.Numeric.hexStringToByteArray(encodedParameters));

                        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                            DelegateeCoordinator.FUNC_NODERESPONDED,
                            Arrays.asList(new org.web3j.abi.datatypes.generated.Bytes32(hashedKey)),
                            Collections.singletonList(new TypeReference<Bool>() {})
                        );
                        String encodedFunction = FunctionEncoder.encode(function);

                        DefaultBlockParameter blockParameter = (blockNumber != null && blockNumber > 0)
                            ? DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber))
                            : DefaultBlockParameter.valueOf("latest");

                        org.web3j.protocol.core.methods.request.Transaction transaction =
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                                credentials.getAddress(),
                                contract.getContractAddress(),
                                encodedFunction
                            );

                        String result = web3j.ethCall(transaction, blockParameter).send().getValue();

                        // Handle empty results from eth_call to prevent decoding errors
                        if (result == null || result.equals("0x") || result.isEmpty()) {
                            log.warn(
                                "getNodeHasDeliveredResponse received empty result for subId {}, interval {}. Assuming false.",
                                subscriptionId,
                                interval
                            );
                            return false;
                        }

                        return (Boolean) FunctionReturnDecoder.decode(result, function.getOutputParameters()).get(0).getValue();
                    } catch (Exception e) {
                        log.error("Failed to check node response status", e);
                        throw new RuntimeException("Failed to check node response status", e);
                    }
                })
            );
    }

    /**
     * Gets the number of responses (redundancy count) for a subscription interval at a specific block.
     *
     * @param subscriptionId The ID of the subscription.
     * @param interval       The interval number.
     * @param blockNumber    The block number to query at. If null, queries the latest block.
     * @return A CompletableFuture containing the redundancy count.
     */
    public CompletableFuture<Integer> getSubscriptionResponseCount(long subscriptionId, long interval, Long blockNumber) {
        return checkContractLoaded()
            .thenCompose(contract ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // The key for the redundancyCount mapping is keccak256(abi.encode(subscriptionId, interval))
                        final org.web3j.abi.datatypes.Function dummyFunction = new org.web3j.abi.datatypes.Function(
                            "dummy",
                            Arrays.asList(new Uint(BigInteger.valueOf(subscriptionId)), new Uint(BigInteger.valueOf(interval))),
                            java.util.Collections.emptyList()
                        );
                        String encodedParameters = FunctionEncoder.encode(dummyFunction).substring(10);
                        byte[] hashedKey = Hash.sha3(org.web3j.utils.Numeric.hexStringToByteArray(encodedParameters));

                        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                            DelegateeCoordinator.FUNC_REDUNDANCYCOUNT,
                            Arrays.asList(new org.web3j.abi.datatypes.generated.Bytes32(hashedKey)),
                            Collections.singletonList(new TypeReference<Uint>() {})
                        );
                        String encodedFunction = FunctionEncoder.encode(function);

                        DefaultBlockParameter blockParameter = (blockNumber != null && blockNumber > 0)
                            ? DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber))
                            : DefaultBlockParameter.valueOf("latest");

                        org.web3j.protocol.core.methods.request.Transaction transaction =
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                                credentials.getAddress(),
                                contract.getContractAddress(),
                                encodedFunction
                            );

                        String result = web3j.ethCall(transaction, blockParameter).send().getValue();

                        // Handle empty results from eth_call to prevent decoding errors
                        if (result == null || result.equals("0x") || result.isEmpty()) {
                            log.warn(
                                "getSubscriptionResponseCount received empty result for subId {}, interval {}. Assuming 0.",
                                subscriptionId,
                                interval
                            );
                            return 0;
                        }

                        return (
                            (BigInteger) FunctionReturnDecoder.decode(result, function.getOutputParameters()).get(0).getValue()
                        ).intValue();
                    } catch (Exception e) {
                        log.error("Failed to get subscription response count", e);
                        throw new RuntimeException("Failed to get subscription response count", e);
                    }
                })
            );
    }

    public CompletableFuture<String> getClient() {
        return checkContractLoaded().thenCompose(c -> c.client().sendAsync());
    }

    public CompletableFuture<String> getSubscriptionBatchReader() {
        return checkContractLoaded().thenCompose(c -> c.getSubscriptionBatchReader().sendAsync());
    }

    public CompletableFuture<Boolean> hasNodeResponded(byte[] requestId) {
        return checkContractLoaded().thenCompose(c -> c.nodeResponded(requestId).sendAsync());
    }

    public CompletableFuture<BigInteger> getRedundancyCount(byte[] requestId) {
        return checkContractLoaded().thenCompose(c -> c.redundancyCount(requestId).sendAsync());
    }

    public CompletableFuture<byte[]> getRequestCommitment(byte[] requestId) {
        return checkContractLoaded().thenCompose(c -> c.requestCommitments(requestId).sendAsync());
    }

    /**
     * Gets the commitment for a specific request ID from the DelegateeCoordinator contract.
     *
     * @param subscriptionId The ID of the subscription.
     * @param interval
     * @return A CompletableFuture containing the commitment as a byte array.
     */
    public CompletableFuture<DelegateeCoordinator.Commitment> getCommitment(long subscriptionId, long interval) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DelegateeCoordinator.Commitment result = delegateeCoordinatorContract
                    .getCommitment(BigInteger.valueOf(subscriptionId), BigInteger.valueOf(interval))
                    .send();
                // Handle empty results from eth_call to prevent decoding errors
                if (result == null) {
                    log.warn("getCommitment received empty result for subId {}, interval {}. Returning null.", subscriptionId, interval);
                    return null; // Return null or an empty Commitment object
                }
                return result;
            } catch (Exception e) {
                log.error("Failed to get commitment for subscription ID {} and interval ID {}", subscriptionId, interval, e);
                throw new RuntimeException("Failed to get commitment", e);
            }
        });
    }

    public CompletableFuture<String> getTypeAndVersion() {
        return checkContractLoaded().thenCompose(c -> c.typeAndVersion().sendAsync());
    }

    /**
     * Checks if the contract is loaded and returns it, or a failed future if not.
     *
     * @return A CompletableFuture with the loaded contract instance.
     */
    private CompletableFuture<DelegateeCoordinator> checkContractLoaded() {
        if (delegateeCoordinatorContract == null) {
            log.error("DelegateeCoordinator contract is not loaded. Cannot proceed.");
            return CompletableFuture.failedFuture(new IllegalStateException("DelegateeCoordinator contract not initialized."));
        }
        return CompletableFuture.completedFuture(delegateeCoordinatorContract);
    }

    /**
     * Returns the loaded address of the DelegateeCoordinator contract.
     *
     * @return The contract address as a string, or null if not loaded.
     */
    public String getContractAddress() {
        if (delegateeCoordinatorContract != null) {
            return delegateeCoordinatorContract.getContractAddress();
        }
        return null;
    }
}

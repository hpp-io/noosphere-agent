package io.hpp.noosphere.agent.service.blockchain.web3;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.DelegateeCoordinator;
import io.hpp.noosphere.agent.contracts.Router;
import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple8;

@Slf4j
@Service
@RequiredArgsConstructor
public class Web3DelegateeCoordinatorService {

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
            String contractAddress = web3RouterService.getCachedContractAddress("Coordinator_v1.0.0");

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

    /**
     * Get a specific compute subscription by its ID from the Router.
     * Note: Subscription data is stored in the Router, not the Coordinator.
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

    //<editor-fold desc="Transaction Functions">
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
        String nodeWallet
    ) {
        return checkContractLoaded()
            .thenCompose(c -> c.reportComputeResult(deliveryInterval, input, output, proof, commitmentData, nodeWallet).sendAsync());
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

    public CompletableFuture<TransactionReceipt> reportVerificationResult(
        BigInteger subscriptionId,
        BigInteger interval,
        String node,
        Boolean valid
    ) {
        return checkContractLoaded().thenCompose(c -> c.reportVerificationResult(subscriptionId, interval, node, valid).sendAsync());
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

    //</editor-fold>

    //<editor-fold desc="View/Pure Functions">
    public CompletableFuture<String> getClient() {
        return checkContractLoaded().thenCompose(c -> c.client().sendAsync());
    }

    public CompletableFuture<String> getSubscriptionBatchReader() {
        return checkContractLoaded().thenCompose(c -> c.getSubscriptionBatchReader().sendAsync());
    }

    public CompletableFuture<Boolean> hasNodeResponded(byte[] requestId) {
        return checkContractLoaded().thenCompose(c -> c.nodeResponded(requestId).sendAsync());
    }

    public CompletableFuture<Tuple8<BigInteger, byte[], String, String, BigInteger, String, BigInteger, BigInteger>> getProofRequest(
        byte[] proofId
    ) {
        return checkContractLoaded().thenCompose(c -> c.proofRequests(proofId).sendAsync());
    }

    public CompletableFuture<BigInteger> getRedundancyCount(byte[] requestId) {
        return checkContractLoaded().thenCompose(c -> c.redundancyCount(requestId).sendAsync());
    }

    public CompletableFuture<byte[]> getRequestCommitment(byte[] requestId) {
        return checkContractLoaded().thenCompose(c -> c.requestCommitments(requestId).sendAsync());
    }

    public CompletableFuture<String> getTypeAndVersion() {
        return checkContractLoaded().thenCompose(c -> c.typeAndVersion().sendAsync());
    }

    //</editor-fold>

    /**
     * Checks if the contract is loaded and returns it, or a failed future if not.
     * @return A CompletableFuture with the loaded contract instance.
     */
    private CompletableFuture<DelegateeCoordinator> checkContractLoaded() {
        if (delegateeCoordinatorContract == null) {
            log.error("DelegateeCoordinator contract is not loaded. Cannot proceed.");
            return CompletableFuture.failedFuture(new IllegalStateException("DelegateeCoordinator contract not initialized."));
        }
        return CompletableFuture.completedFuture(delegateeCoordinatorContract);
    }
}

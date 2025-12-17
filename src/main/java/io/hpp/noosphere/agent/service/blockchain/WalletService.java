package io.hpp.noosphere.agent.service.blockchain;

import static io.hpp.noosphere.agent.config.Constants.ZERO_ADDRESS;

import com.google.common.base.Supplier;
import io.hpp.noosphere.agent.config.ApplicationProperties;
import io.hpp.noosphere.agent.service.NoosphereConfigService;
import io.hpp.noosphere.agent.service.blockchain.dto.SignatureParamsDTO;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3DelegateeCoordinatorService;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private record SimulationResult(boolean success, boolean needsGasEstimation) {}

    private final Web3j web3j;
    private final Web3DelegateeCoordinatorService coordinatorService;
    private final ApplicationProperties.NoosphereConfig.Chain.Wallet walletProperties;
    private final Credentials credentials;
    private final TransactionManager transactionManager;
    private final TransactionReceiptProcessor transactionReceiptProcessor;

    // Lock to prevent race conditions when sending multiple transactions
    private final ReentrantLock txLock = new ReentrantLock();

    public WalletService(
        Web3j web3j,
        Web3DelegateeCoordinatorService coordinatorService,
        NoosphereConfigService noosphereConfigService,
        Credentials credentials,
        BigInteger chainId
    ) throws IOException {
        walletProperties = noosphereConfigService.getActiveConfig().getChain().getWallet();
        this.coordinatorService = coordinatorService;
        this.web3j = web3j;
        this.credentials = credentials;
        this.transactionManager = new RawTransactionManager(web3j, credentials, chainId.longValue());
        this.transactionReceiptProcessor = new PollingTransactionReceiptProcessor(web3j, 1000, 15); // Poll every 1s, 15 attempts

        log.info("Initialized WalletService for address: {}", getAddress());
    }

    public String getAddress() {
        return credentials.getAddress();
    }

    public String getPaymentAddress() {
        return walletProperties.getPaymentAddress() != null ? walletProperties.getPaymentAddress() : ZERO_ADDRESS;
    }

    public CompletableFuture<String> deliverCompute(
        SubscriptionDTO subscription,
        long interval,
        byte[] input,
        byte[] output,
        byte[] proof,
        byte[] commitmentData
    ) {
        // Supplier for the simulation logic
        Supplier<CompletableFuture<Void>> simulationSupplier = () ->
            coordinatorService.simulateReportComputeResult(
                BigInteger.valueOf(interval),
                input,
                output,
                proof,
                commitmentData,
                getPaymentAddress()
            );

        // Supplier for the actual transaction data
        Supplier<String> txDataSupplier = () ->
            coordinatorService.getReportComputeResultTxData(
                BigInteger.valueOf(interval),
                input,
                output,
                proof,
                commitmentData,
                getPaymentAddress()
            );

        return simulateAndSend(simulationSupplier, txDataSupplier, subscription);
    }

    public CompletableFuture<String> deliverComputeDelegatee(
        SubscriptionDTO subscription,
        SignatureParamsDTO signature,
        byte[] input,
        byte[] output,
        byte[] proof
    ) {
        // Supplier for the simulation logic
        Supplier<CompletableFuture<Void>> simulationSupplier = () ->
            coordinatorService.simulateReportDelegatedComputeResult(
                subscription,
                signature,
                BigInteger.valueOf(subscription.getInterval()),
                input,
                output,
                proof,
                getPaymentAddress()
            );

        // Supplier for the actual transaction data
        Supplier<String> txDataSupplier = () ->
            coordinatorService.getReportDelegatedComputeResultTxData(
                subscription,
                signature,
                BigInteger.valueOf(subscription.getInterval()),
                input,
                output,
                proof,
                getPaymentAddress()
            );

        return simulateAndSend(simulationSupplier, txDataSupplier, subscription);
    }

    /**
     * A generic method to first simulate and then send a transaction.
     *
     * @param simulationSupplier A supplier for the async simulation task.
     * @param txDataSupplier     A supplier for the transaction data payload.
     * @param subscription       The context subscription for logging.
     * @return A CompletableFuture containing the transaction hash.
     */
    private CompletableFuture<String> simulateAndSend(
        Supplier<CompletableFuture<Void>> simulationSupplier,
        Supplier<String> txDataSupplier,
        SubscriptionDTO subscription
    ) {
        // First, run the simulation with retries
        return trySimulation(simulationSupplier, subscription).thenCompose(simulationResult -> {
            // If simulation was successful, proceed to send the transaction
            if (simulationResult.success()) {
                Long gasLimit = simulationResult.needsGasEstimation() ? null : walletProperties.getMaxGasLimit().longValue();
                return sendTransactionWithLock(txDataSupplier, gasLimit);
            } else {
                // If simulation failed permanently, complete with an exception
                return CompletableFuture.failedFuture(new RuntimeException("Transaction simulation failed permanently."));
            }
        });
    }

    /**
     * Tries to run the simulation up to 3 times.
     *
     * @param simulationSupplier A supplier for the async simulation task.
     * @param subscription       The context subscription for logging.
     * @return A CompletableFuture with a SimulationResult.
     */
    private CompletableFuture<SimulationResult> trySimulation(
        Supplier<CompletableFuture<Void>> simulationSupplier,
        SubscriptionDTO subscription
    ) {
        return CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    log.debug("Simulation attempt {} for subscription {}", i + 1, subscription.getId());
                    simulationSupplier.get().join(); // Execute and wait for simulation
                    log.info("Simulation successful for subscription {}", subscription.getId());
                    return new SimulationResult(true, true); // Success, needs gas estimation
                } catch (Exception e) {
                    log.error("Simulation attempt {} error for subscription {}: {}", i + 1, subscription.getId(), e.getMessage(), e);
                    // Check for allowed simulation errors
                    for (String allowedError : walletProperties.getAllowedSimErrors()) {
                        if (e.getMessage() != null && e.getMessage().toLowerCase().contains(allowedError.toLowerCase())) {
                            log.warn("Bypassing simulation error for subscription {}: {}", subscription.getId(), e.getMessage());
                            return new SimulationResult(true, false); // Success, but bypassed, use fixed gas
                        }
                    }
                    log.warn("Simulation attempt {} failed for {}: {}", i + 1, subscription.getId(), e.getMessage());
                    if (i == 2) { // Last attempt
                        log.error("Transaction simulation failed after 3 attempts for subscription {}", subscription.getId(), e);
                        throw new RuntimeException("Transaction simulation failed after 3 attempts", e);
                    }
                    // Wait before retrying
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                }
            }
            throw new IllegalStateException("Simulation loop finished unexpectedly.");
        });
    }

    /**
     * Acquires a lock and sends a transaction to prevent nonce collisions.
     * @param txDataSupplier A supplier for the transaction data payload.
     * @param gasLimit A specific gas limit, or null to let web3j estimate it.
     * @return A CompletableFuture containing the transaction hash.
     */
    private CompletableFuture<String> sendTransactionWithLock(Supplier<String> txDataSupplier, Long gasLimit) {
        return CompletableFuture.supplyAsync(() -> {
            txLock.lock();
            try {
                String to = coordinatorService.getContractAddress() != null ? coordinatorService.getContractAddress() : ZERO_ADDRESS;
                String data = txDataSupplier.get();
                log.debug("Transaction data prepared, length: {}", data != null ? data.length() : 0);

                log.debug("Fetching gas price...");
                BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
                log.debug("Gas price: {}", gasPrice);
                BigInteger finalGasLimit;

                if (gasLimit != null) {
                    finalGasLimit = BigInteger.valueOf(gasLimit);
                    log.debug("Using provided gas limit: {}", finalGasLimit);
                } else {
                    // Estimate gas if not provided
                    log.debug("Estimating gas...");
                    var estimateResponse = web3j
                        .ethEstimateGas(
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(getAddress(), to, data)
                        )
                        .send();

                    if (estimateResponse.hasError()) {
                        String errorMsg = estimateResponse.getError().getMessage();
                        log.warn("Gas estimation failed: {}", errorMsg);

                        // Check if this is an allowed error - if so, use fixed gas limit
                        boolean isAllowedError = false;
                        for (String allowedError : walletProperties.getAllowedSimErrors()) {
                            if (errorMsg != null && errorMsg.toLowerCase().contains(allowedError.toLowerCase())) {
                                log.info("Bypassing gas estimation error (using fixed gas limit): {}", errorMsg);
                                isAllowedError = true;
                                break;
                            }
                        }

                        if (isAllowedError) {
                            finalGasLimit = BigInteger.valueOf(walletProperties.getMaxGasLimit());
                        } else {
                            throw new IOException("Gas estimation error: " + errorMsg);
                        }
                    } else {
                        log.debug("Gas estimate response: {}", estimateResponse.getResult());
                        finalGasLimit = estimateResponse.getAmountUsed();
                        log.debug("Estimated gas: {}", finalGasLimit);
                    }
                }

                log.debug("Sending transaction to: {} with gas price: {}, gas limit: {}", to, gasPrice, finalGasLimit);
                EthSendTransaction ethSendTransaction = transactionManager.sendTransaction(
                    gasPrice,
                    finalGasLimit,
                    to,
                    data,
                    BigInteger.ZERO // value
                );

                if (ethSendTransaction.hasError()) {
                    throw new IOException("Error sending transaction: " + ethSendTransaction.getError().getMessage());
                }

                String txHash = ethSendTransaction.getTransactionHash();
                log.debug("Transaction sent, hash: {}, waiting for receipt...", txHash);
                TransactionReceipt receipt = transactionReceiptProcessor.waitForTransactionReceipt(txHash);

                if (!receipt.isStatusOK()) {
                    throw new TransactionException("Transaction failed with status: " + receipt.getStatus(), receipt);
                }
                return receipt.getTransactionHash();
            } catch (IOException | TransactionException e) {
                log.error("Error sending transaction: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send transaction: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected error sending transaction: {}", e.getMessage(), e);
                throw new RuntimeException("Unexpected error sending transaction: " + e.getMessage(), e);
            } finally {
                txLock.unlock();
            }
        });
    }
}

package io.hpp.noosphere.agent.service.blockchain;

import static io.hpp.noosphere.agent.config.Constants.ZERO_ADDRESS;

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

    /**
     * Simulates a transaction call and retries on failure.
     * @param contractFunction The contract function to simulate.
     * @param subscription The context subscription for logging.
     * @return A CompletableFuture that completes with true if the simulation was bypassed due to an allowed error, false otherwise.
     */
    private CompletableFuture<Boolean> simulateTransaction(Runnable contractFunction, SubscriptionDTO subscription) {
        return CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    contractFunction.run();
                    // If it runs without throwing, simulation is successful
                    return false;
                } catch (Exception e) {
                    // Check for allowed simulation errors
                    for (String allowedError : walletProperties.getAllowedSimErrors()) {
                        if (e.getMessage().toLowerCase().contains(allowedError.toLowerCase())) {
                            log.warn("Bypassing simulation error for subscription {}: {}", subscription.getId(), e.getMessage());
                            return true; // Bypassed
                        }
                    }
                    // If not an allowed error, log and prepare for retry
                    log.warn(
                        "Transaction simulation failed on attempt {} for subscription {}: {}",
                        i + 1,
                        subscription.getId(),
                        e.getMessage()
                    );
                    if (i == 2) { // Last attempt
                        // In a real scenario, you might want to re-throw a specific exception
                        // For simplicity, we'll re-throw the last caught exception.
                        throw new RuntimeException("Transaction simulation failed after 3 attempts", e);
                    }
                    try {
                        Thread.sleep(500); // Delay before retry
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(interruptedException);
                    }
                }
            }
            // Should not be reached
            throw new IllegalStateException("Simulation loop finished without returning a value.");
        });
    }

    public CompletableFuture<String> deliverCompute(
        SubscriptionDTO subscription,
        byte[] input,
        byte[] output,
        byte[] proof,
        byte[] commitmentData
    ) {
        Runnable simulation = () -> {
            try {
                coordinatorService
                    .reportComputeResult(
                        BigInteger.valueOf(subscription.getInterval()),
                        input,
                        output,
                        proof,
                        commitmentData,
                        getPaymentAddress()
                    )
                    .join(); // .send() on a read-only call simulates it
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        return simulateTransaction(simulation, subscription).thenCompose(skipped -> {
            if (skipped) {
                // If simulation was skipped due to an allowed error, send with a fixed gas limit
                return sendTransactionWithLock(
                    () ->
                        coordinatorService.getReportComputeResultTxData(
                            BigInteger.valueOf(subscription.getInterval()),
                            input,
                            output,
                            proof,
                            commitmentData,
                            getPaymentAddress()
                        ),
                    walletProperties.getMaxGasLimit().longValue()
                );
            } else {
                // Otherwise, let web3j estimate the gas
                return sendTransactionWithLock(
                    () ->
                        coordinatorService.getReportComputeResultTxData(
                            BigInteger.valueOf(subscription.getInterval()),
                            input,
                            output,
                            proof,
                            commitmentData,
                            getPaymentAddress()
                        ),
                    null // Gas limit will be estimated
                );
            }
        });
    }

    public CompletableFuture<String> deliverComputeDelegatee(
        SubscriptionDTO subscription,
        SignatureParamsDTO signature,
        byte[] input,
        byte[] output,
        byte[] proof
    ) {
        Runnable simulation = () -> {
            try {
                coordinatorService
                    .simulateReportDelegatedComputeResult(
                        subscription,
                        signature,
                        BigInteger.valueOf(subscription.getInterval()),
                        input,
                        output,
                        proof,
                        getPaymentAddress()
                    )
                    .join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        return simulateTransaction(simulation, subscription).thenCompose(skipped -> {
            java.util.function.Supplier<String> txDataSupplier = () ->
                coordinatorService.getReportDelegatedComputeResultTxData(
                    subscription,
                    signature,
                    BigInteger.valueOf(subscription.getInterval()),
                    input,
                    output,
                    proof,
                    getPaymentAddress()
                );

            if (skipped) {
                return sendTransactionWithLock(txDataSupplier, walletProperties.getMaxGasLimit().longValue());
            } else {
                return sendTransactionWithLock(txDataSupplier, null);
            }
        });
    }

    /**
     * Acquires a lock and sends a transaction to prevent nonce collisions.
     * @param txDataSupplier A supplier for the transaction data payload.
     * @param gasLimit A specific gas limit, or null to let web3j estimate it.
     * @return A CompletableFuture containing the transaction hash.
     */
    private CompletableFuture<String> sendTransactionWithLock(java.util.function.Supplier<String> txDataSupplier, Long gasLimit) {
        return CompletableFuture.supplyAsync(() -> {
            txLock.lock();
            try {
                String to = coordinatorService.getContractAddress() != null ? coordinatorService.getContractAddress() : ZERO_ADDRESS;
                String data = txDataSupplier.get();

                BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
                BigInteger finalGasLimit;

                if (gasLimit != null) {
                    finalGasLimit = BigInteger.valueOf(gasLimit);
                } else {
                    // Estimate gas if not provided
                    finalGasLimit = web3j
                        .ethEstimateGas(
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(getAddress(), to, data)
                        )
                        .send()
                        .getAmountUsed();
                }

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
                TransactionReceipt receipt = transactionReceiptProcessor.waitForTransactionReceipt(txHash);

                if (!receipt.isStatusOK()) {
                    throw new TransactionException("Transaction failed with status: " + receipt.getStatus(), receipt);
                }
                return receipt.getTransactionHash();
            } catch (IOException | TransactionException e) {
                log.error("Error sending transaction", e);
                throw new RuntimeException("Failed to send transaction", e);
            } finally {
                txLock.unlock();
            }
        });
    }
}

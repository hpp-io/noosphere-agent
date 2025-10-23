package io.hpp.noosphere.agent.service.blockchain.web3;

import static io.hpp.noosphere.agent.config.Constants.ZERO_ADDRESS;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.SubscriptionBatchReader;
import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;

@Service
public class Web3SubscriptionBatchReaderService {

    private static final Logger log = LoggerFactory.getLogger(Web3SubscriptionBatchReaderService.class);

    private final Web3DelegateeCoordinatorService web3DelegateeCoordinatorService;
    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    private SubscriptionBatchReader subscriptionBatchReaderContract;

    public Web3SubscriptionBatchReaderService(
        Web3DelegateeCoordinatorService web3DelegateeCoordinatorService,
        Web3j web3j,
        Credentials credentials,
        Web3jConfig.CustomGasProvider gasProvider
    ) {
        this.web3DelegateeCoordinatorService = web3DelegateeCoordinatorService;
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    @PostConstruct
    public void init() {
        try {
            String contractAddress = web3DelegateeCoordinatorService.getSubscriptionBatchReader().join();

            if (contractAddress == null || contractAddress.isEmpty() || contractAddress.equals(ZERO_ADDRESS)) {
                throw new IllegalStateException("SubscriptionBatchReader contract address not found.");
            }

            this.subscriptionBatchReaderContract = SubscriptionBatchReader.load(contractAddress, web3j, credentials, gasProvider);
            log.info("Loaded SubscriptionBatchReader contract at address: {}", contractAddress);
        } catch (Exception e) {
            log.error("Failed to initialize Web3SubscriptionBatchReaderService", e);
            throw new RuntimeException("Could not initialize Web3SubscriptionBatchReaderService", e);
        }
    }

    /**
     * Reads a batch of subscriptions from the SubscriptionBatchReader contract at a specific block.
     *
     * @param startId     The starting subscription ID.
     * @param endId       The ending subscription ID.
     * @param blockNumber The block number to query at.
     * @return A CompletableFuture containing a list of ComputeSubscription objects.
     */
    public CompletableFuture<List<SubscriptionBatchReader.ComputeSubscription>> getSubscriptions(int startId, int endId, long blockNumber) {
        return checkContractLoaded()
            .thenCompose(contract -> {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("Reading subscription batch from {} to {} at block {}", startId, endId, blockNumber);

                        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                            SubscriptionBatchReader.FUNC_GETSUBSCRIPTIONS,
                            Arrays.asList(new Uint64(startId), new Uint64(endId)),
                            Collections.singletonList(new TypeReference<DynamicArray<SubscriptionBatchReader.ComputeSubscription>>() {})
                        );
                        String encodedFunction = FunctionEncoder.encode(function);
                        DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber));

                        org.web3j.protocol.core.methods.request.Transaction transaction =
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                                credentials.getAddress(),
                                contract.getContractAddress(),
                                encodedFunction
                            );

                        String result = web3j.ethCall(transaction, blockParam).send().getValue();
                        List<SubscriptionBatchReader.ComputeSubscription> subscriptions = (List<
                                SubscriptionBatchReader.ComputeSubscription
                            >) FunctionReturnDecoder.decode(result, function.getOutputParameters()).get(0).getValue();
                        return subscriptions;
                    } catch (Exception e) {
                        log.error("Failed to get subscriptions for batch [{}-{}]", startId, endId, e);
                        throw new RuntimeException("Failed to get subscriptions", e);
                    }
                });
            });
    }

    /**
     * Reads the interval statuses (redundancy count, commitment existence) for a batch of subscriptions.
     *
     * @param subscriptionIds The list of subscription IDs.
     * @param intervals       The list of corresponding intervals.
     * @param blockNumber     The block number to query at.
     * @return A CompletableFuture containing a list of IntervalStatus objects.
     */
    public CompletableFuture<List<SubscriptionBatchReader.IntervalStatus>> getIntervalStatuses(
        List<Long> subscriptionIds,
        List<Integer> intervals,
        long blockNumber
    ) {
        return checkContractLoaded()
            .thenCompose(contract -> {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("Reading interval statuses for {} subscriptions at block {}", subscriptionIds.size(), blockNumber);

                        List<Uint64> subIdBigInts = subscriptionIds.stream().map(Uint64::new).collect(Collectors.toList());
                        List<Uint32> intervalBigInts = intervals.stream().map(Uint32::new).collect(Collectors.toList());

                        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                            SubscriptionBatchReader.FUNC_GETINTERVALSTATUSES,
                            Arrays.asList(
                                new DynamicArray<>(Uint64.class, subIdBigInts),
                                new DynamicArray<>(Uint32.class, intervalBigInts)
                            ),
                            Collections.singletonList(new TypeReference<DynamicArray<SubscriptionBatchReader.IntervalStatus>>() {})
                        );
                        String encodedFunction = FunctionEncoder.encode(function);
                        DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber));

                        org.web3j.protocol.core.methods.request.Transaction transaction =
                            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                                credentials.getAddress(),
                                contract.getContractAddress(),
                                encodedFunction
                            );

                        String result = web3j.ethCall(transaction, blockParam).send().getValue();
                        List<SubscriptionBatchReader.IntervalStatus> statuses = (List<
                                SubscriptionBatchReader.IntervalStatus
                            >) FunctionReturnDecoder.decode(result, function.getOutputParameters()).get(0).getValue();
                        return statuses;
                    } catch (Exception e) {
                        log.error("Failed to get interval statuses", e);
                        throw new RuntimeException("Failed to get interval statuses", e);
                    }
                });
            });
    }

    /**
     * Checks if the contract is loaded and returns it, or a failed future if not.
     * @return A CompletableFuture with the loaded contract instance.
     */
    private CompletableFuture<SubscriptionBatchReader> checkContractLoaded() {
        if (subscriptionBatchReaderContract == null) {
            log.error("SubscriptionBatchReader contract is not loaded. Cannot proceed.");
            return CompletableFuture.failedFuture(new IllegalStateException("SubscriptionBatchReader contract not initialized."));
        }
        return CompletableFuture.completedFuture(subscriptionBatchReaderContract);
    }
}

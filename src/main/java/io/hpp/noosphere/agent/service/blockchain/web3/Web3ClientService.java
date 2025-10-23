package io.hpp.noosphere.agent.service.blockchain.web3;

import static io.hpp.noosphere.agent.config.Constants.ZERO_ADDRESS;

import io.hpp.noosphere.agent.config.Web3jConfig;
import io.hpp.noosphere.agent.contracts.Delegator;
import io.hpp.noosphere.agent.contracts.TransientComputeClient;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;

@Service
public class Web3ClientService {

    private static final Logger log = LoggerFactory.getLogger(Web3ClientService.class);

    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jConfig.CustomGasProvider gasProvider;

    public Web3ClientService(Web3j web3j, Credentials credentials, Web3jConfig.CustomGasProvider gasProvider) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /**
     * Dynamically loads a TransientComputeClient contract and calls getComputeInputs.
     *
     * @param clientAddress The address of the TransientComputeClient contract.
     * @param subscriptionId The ID of the subscription.
     * @param interval The interval number.
     * @param timestamp The current timestamp.
     * @return A CompletableFuture containing the compute inputs as a byte array.
     */
    public CompletableFuture<byte[]> getComputeInputs(
        String clientAddress,
        BigInteger subscriptionId,
        BigInteger interval,
        BigInteger timestamp
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
                log.debug("Calling getComputeInputs on contract {} for subId {}", clientAddress, subscriptionId);
                // The 'caller' is the agent's own address
                return clientContract.getComputeInputs(subscriptionId, interval, timestamp, credentials.getAddress()).send();
            } catch (Exception e) {
                log.error("Failed to call getComputeInputs for client {}", clientAddress, e);
                throw new RuntimeException("Failed to get compute inputs", e);
            }
        });
    }

    public CompletableFuture<TransactionReceipt> createComputeSubscription(
        String clientAddress,
        String containerId,
        BigInteger maxExecutions,
        BigInteger intervalSeconds,
        BigInteger redundancy,
        Boolean useDeliveryInbox,
        String feeToken,
        BigInteger feeAmount,
        String wallet,
        String verifier,
        byte[] routeId
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
                return clientContract
                    .createComputeSubscription(
                        containerId,
                        maxExecutions,
                        intervalSeconds,
                        redundancy,
                        useDeliveryInbox,
                        feeToken,
                        feeAmount,
                        wallet,
                        verifier,
                        routeId
                    )
                    .send();
            } catch (Exception e) {
                log.error("Failed to call createComputeSubscription for client {}", clientAddress, e);
                throw new RuntimeException("Failed to create compute subscription", e);
            }
        });
    }

    public CompletableFuture<TransactionReceipt> receiveRequestCompute(
        String clientAddress,
        BigInteger subscriptionId,
        BigInteger interval,
        BigInteger numRedundantDeliveries,
        Boolean useDeliveryInbox,
        String node,
        byte[] input,
        byte[] output,
        byte[] proof,
        byte[] containerId
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
                return clientContract
                    .receiveRequestCompute(
                        subscriptionId,
                        interval,
                        numRedundantDeliveries,
                        useDeliveryInbox,
                        node,
                        input,
                        output,
                        proof,
                        containerId
                    )
                    .send();
            } catch (Exception e) {
                log.error("Failed to call receiveRequestCompute for client {}", clientAddress, e);
                throw new RuntimeException("Failed to receive request compute", e);
            }
        });
    }

    public CompletableFuture<TransactionReceipt> sendRequest(String clientAddress, BigInteger subscriptionId, BigInteger interval) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
                return clientContract.sendRequest(subscriptionId, interval).send();
            } catch (Exception e) {
                log.error("Failed to call sendRequest for client {}", clientAddress, e);
                throw new RuntimeException("Failed to send request", e);
            }
        });
    }

    public CompletableFuture<Tuple2<Boolean, TransientComputeClient.PendingDelivery>> getDelivery(
        String clientAddress,
        byte[] requestId,
        String node
    ) {
        TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
        return clientContract.getDelivery(requestId, node).sendAsync();
    }

    public CompletableFuture<List> getNodesForRequest(String clientAddress, byte[] requestId) {
        TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
        return clientContract.getNodesForRequest(requestId).sendAsync();
    }

    public CompletableFuture<Boolean> hasDelivery(String clientAddress, byte[] requestId, String node) {
        TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
        return clientContract.hasDelivery(requestId, node).sendAsync();
    }

    public CompletableFuture<String> typeAndVersion(String clientAddress) {
        TransientComputeClient clientContract = TransientComputeClient.load(clientAddress, web3j, credentials, gasProvider);
        return clientContract.typeAndVersion().sendAsync();
    }

    /**
     * Fetches the delegated signer from a subscription consumer contract.
     */
    public CompletableFuture<String> getDelegatedSigner(SubscriptionDTO subscription, Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                    Delegator.FUNC_GETSIGNER,
                    java.util.Collections.emptyList(),
                    java.util.Collections.singletonList(new TypeReference<Address>() {})
                );
                String encodedFunction = FunctionEncoder.encode(function);

                DefaultBlockParameter blockParameter;
                if (blockNumber != null && blockNumber > 0) {
                    log.debug("Querying delegated signer for client {} at block {}", subscription.getClient(), blockNumber);
                    blockParameter = DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber));
                } else {
                    log.debug("Querying delegated signer for client {} at latest block...", subscription.getClient());
                    blockParameter = DefaultBlockParameter.valueOf("latest");
                }

                org.web3j.protocol.core.methods.request.Transaction transaction =
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        credentials.getAddress(),
                        subscription.getClient(), // The delegator contract address is the client/owner
                        encodedFunction
                    );

                String result = web3j.ethCall(transaction, blockParameter).send().getValue();
                String signerAddress = (String) FunctionReturnDecoder.decode(result, function.getOutputParameters()).get(0).getValue();

                log.debug("Fetched delegated signer: {}", signerAddress);
                return signerAddress;
            } catch (Exception e) {
                log.warn(
                    "Failed to get delegated signer for client {}. Returning zero address. Error: {}",
                    subscription.getClient(),
                    e.getMessage()
                );
                return ZERO_ADDRESS;
            }
        });
    }
}

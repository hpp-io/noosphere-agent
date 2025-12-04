package io.hpp.noosphere.agent.service.blockchain;

import io.hpp.noosphere.agent.contracts.DelegateeCoordinator;
import io.hpp.noosphere.agent.contracts.Router;
import io.hpp.noosphere.agent.service.blockchain.dto.ExistingDelegateSubscription;
import io.hpp.noosphere.agent.service.blockchain.dto.SignatureParamsDTO;
import io.hpp.noosphere.agent.service.blockchain.dto.TxStatus;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3ClientService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3DelegateeCoordinatorService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3DelegatorService;
import io.hpp.noosphere.agent.service.blockchain.web3.Web3RouterService;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.generated.*;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Numeric;

@Service
public class CoordinatorService {

    private static final Logger log = LoggerFactory.getLogger(CoordinatorService.class);

    private final Web3RouterService web3RouterService;
    private final Web3DelegatorService web3DelegatorService;
    private final Web3DelegateeCoordinatorService web3DelegateeCoordinatorService;
    private final Web3ClientService web3ClientService;
    private final Web3j web3j;

    public CoordinatorService(
        Web3RouterService web3RouterService,
        Web3DelegatorService web3DelegatorService,
        Web3DelegateeCoordinatorService web3DelegateeCoordinatorService,
        Web3ClientService web3ClientService,
        Web3j web3j
    ) {
        this.web3RouterService = web3RouterService;
        this.web3DelegatorService = web3DelegatorService;
        this.web3DelegateeCoordinatorService = web3DelegateeCoordinatorService;
        this.web3ClientService = web3ClientService;
        this.web3j = web3j;
    }

    /**
     * Fetches the delegated signer from a subscription consumer contract.
     */
    public CompletableFuture<String> getDelegatedSigner(SubscriptionDTO subscription, Long blockNumber) {
        return web3DelegatorService.getDelegatedSigner(subscription, blockNumber);
    }

    /**
     * Checks if a DelegateSubscription has already created an on-chain subscription.
     */
    public CompletableFuture<ExistingDelegateSubscription> getExistingDelegateSubscription(
        SubscriptionDTO subscription,
        SignatureParamsDTO signature,
        long blockNumber
    ) {
        return web3RouterService
            .getDelegateCreatedId(subscription, signature, blockNumber)
            .thenApply(subscriptionId -> {
                boolean exists = !subscriptionId.equals(BigInteger.ZERO);
                return new ExistingDelegateSubscription(exists, subscriptionId.longValue());
            });
    }

    /**
     * Recovers the delegatee signer from a signature.
     */
    public CompletableFuture<String> recoverDelegateeSigner(SubscriptionDTO subscription, SignatureParamsDTO signature) {
        return CompletableFuture.supplyAsync(() -> {
            Sign.SignatureData signatureData = new Sign.SignatureData(
                (byte) signature.v(),
                Numeric.toBytesPadded(signature.r(), 32),
                Numeric.toBytesPadded(signature.s(), 32)
            );

            // Re-create EIP-712 typed data hash
            // This is a simplified version. A full implementation requires building the
            // EIP712-compliant structure and hashing it.
            // For this example, we assume a method `getTypedDataHash` exists.
            byte[] messageHash = subscription.getTypedDataHashForDelegation(
                signature.nonce(),
                signature.expiry(),
                web3RouterService.getChainId(),
                web3RouterService.getCachedContractAddress("Coordinator_v1.0.0")
            );

            // Use signedMessageToKey for EIP-712, as it expects a pre-hashed message
            // and correctly handles the signature data to recover the public key.
            BigInteger recoveredKey = null;
            try {
                recoveredKey = Sign.signedMessageToKey(messageHash, signatureData);
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }

            return "0x" + org.web3j.crypto.Keys.getAddress(recoveredKey);
        });
    }

    /**
     * Returns the latest coordinator subscription ID.
     */
    public CompletableFuture<Long> getHeadSubscriptionId() {
        // Call the correct method on Web3RouterService and convert the result to Long.
        return web3RouterService.getLastSubscriptionId().thenApply(BigInteger::longValue);
    }

    /**
     * Returns a subscription by its ID.
     */
    @Cacheable(value = "subscriptions", key = "{#subscriptionId, #blockParameter.getBlockNumber()}")
    public CompletableFuture<SubscriptionDTO> getSubscriptionById(long subscriptionId) {
        log.debug("Fetching subscription {}", subscriptionId);
        return web3RouterService
            .getComputeSubscription(BigInteger.valueOf(subscriptionId))
            .thenApply(contractSub -> {
                SubscriptionDTO dto = convertContractSubToDTO(contractSub);
                if (dto != null) {
                    dto.setId(subscriptionId);
                }
                return dto;
            });
    }

    /**
     * Returns container inputs for a subscription.
     */
    public CompletableFuture<byte[]> getContainerInputs(SubscriptionDTO subscription, long interval) {
        // The timestamp is generated now, and the caller is the agent's address.
        // These details should be handled by the calling service or within this method.
        long timestamp = Instant.now().getEpochSecond();

        return web3ClientService
            .getComputeInputs(
                subscription.getClient(),
                BigInteger.valueOf(subscription.getId()),
                BigInteger.valueOf(interval),
                BigInteger.valueOf(timestamp)
            )
            .exceptionally(e -> {
                log.warn(
                    "Could not get on-chain container inputs for sub {}. Returning empty. Error: {}",
                    subscription.getId(),
                    e.getMessage()
                );
                return "".getBytes();
            });
    }

    /**
     * Gets the commitment for a specific subscription and interval.
     *
     * @param subscriptionId The ID of the subscription.
     * @param interval       The interval number.
     * @return A CompletableFuture containing the commitment as a byte array.
     */
    public CompletableFuture<DelegateeCoordinator.Commitment> getCommitment(long subscriptionId, long interval) {
        return web3DelegateeCoordinatorService.getCommitment(subscriptionId, interval);
    }

    /**
     * Checks if a node has already delivered a response for a given interval.
     */
    public CompletableFuture<Boolean> getNodeHasDeliveredResponse(
        long subscriptionId,
        long interval,
        String nodeAddress,
        Long blockNumber
    ) {
        // Query at the latest block by default.
        return web3DelegateeCoordinatorService.getNodeHasDeliveredResponse(subscriptionId, interval, nodeAddress, blockNumber);
    }

    /**
     * Gets the number of responses for a subscription interval.
     */
    public CompletableFuture<Integer> getSubscriptionResponseCount(long subscriptionId, long interval, Long blockNumber) {
        return web3DelegateeCoordinatorService.getSubscriptionResponseCount(subscriptionId, interval, blockNumber);
    }

    private SubscriptionDTO convertContractSubToDTO(Router.ComputeSubscription contractSub) {
        if (contractSub == null) {
            return null;
        }
        // This mapping needs to be precise and complete based on your SubscriptionDTO definition.
        return SubscriptionDTO.builder()
            .client(contractSub.client)
            .routeId(Numeric.toHexString(contractSub.routeId))
            .containerId(Numeric.toHexString(contractSub.containerId))
            .feeAmount(contractSub.feeAmount)
            .feeToken(contractSub.feeToken)
            .activeAt(contractSub.activeAt.longValue())
            .intervalSeconds(contractSub.intervalSeconds.longValue())
            .maxExecutions(contractSub.maxExecutions.longValue())
            .wallet(contractSub.wallet)
            .verifier(contractSub.verifier)
            .redundancy(contractSub.redundancy.intValue())
            .useDeliveryInbox(contractSub.useDeliveryInbox)
            .build();
    }

    /**
     * Asynchronously collects transaction success status by its hash.
     *
     * @param txHash The transaction hash to check.
     * @return A CompletableFuture containing a TxStatus record.
     */
    public CompletableFuture<TxStatus> getTxSuccess(String txHash) {
        return web3j
            .ethGetTransactionReceipt(txHash)
            .sendAsync()
            .thenApply(ethGetTransactionReceipt -> {
                return ethGetTransactionReceipt
                    .getTransactionReceipt()
                    .map(receipt -> {
                        // Transaction found, return its status
                        log.debug("Receipt found for tx {}: status={}", txHash, receipt.getStatus());
                        return new TxStatus(true, receipt.isStatusOK());
                    })
                    .orElseGet(() -> {
                        // Transaction not found (e.g., not yet mined)
                        log.debug("Receipt not yet found for tx {}", txHash);
                        return new TxStatus(false, false);
                    });
            })
            .exceptionally(e -> {
                log.error("Error fetching receipt for tx {}", txHash, e);
                return new TxStatus(false, false); // Treat errors as not found/failed
            });
    }

    /**
     * Encodes commitment data into a byte array according to the ABI specification,
     * matching `ethers.AbiCoder.defaultAbiCoder().encode()`.
     *
     * @param data The commitment data to encode.
     * @return The ABI-encoded byte array.
     */
    public byte[] encodeCommitment(DelegateeCoordinator.Commitment data) {
        // Create a StaticStruct representing the tuple:
        // (bytes32,uint64,bytes32,uint32,bool,uint16,address,uint256,address,address,address)
        final StaticStruct commitmentStruct = new StaticStruct(
            new Bytes32(data.requestId),
            new Uint64(data.subscriptionId),
            new Bytes32(data.containerId),
            new Uint32(data.interval),
            new Bool(data.useDeliveryInbox),
            new Uint16(data.redundancy),
            new Address(data.walletAddress),
            new Uint256(data.feeAmount),
            new Address(data.feeToken),
            new Address(data.verifier),
            new Address(data.coordinator)
        );

        // To get the raw `abi.encode` value, we create a dummy function
        // with the struct as the single parameter, encode the function call,
        // and then strip the 4-byte function selector.
        final Function dummyFunction = new Function(
            "dummy", // Function name does not affect parameter encoding
            Collections.singletonList(commitmentStruct),
            Collections.emptyList()
        );

        String encodedFunctionCall = FunctionEncoder.encode(dummyFunction);

        // The result of `abi.encode` is just the encoded parameters,
        // so we remove the function selector (first 10 chars: "0x" + 8 hex chars).
        return Numeric.hexStringToByteArray(encodedFunctionCall.substring(10));
    }

    /**
     * Generates a request ID by packing and hashing the subscription ID and interval,
     * equivalent to `keccak256(abi.encodePacked(uint64, uint32))`.
     *
     * @param subscriptionId The subscription ID (uint64).
     * @param interval The interval number (uint32).
     * @return The calculated request ID as a 32-byte array.
     */
    public byte[] getRequestId(BigInteger subscriptionId, BigInteger interval) {
        // Allocate a 12-byte buffer (8 for uint64 + 4 for uint32)
        ByteBuffer buffer = ByteBuffer.allocate(12);

        // Put the subscriptionId as a long (8 bytes)
        buffer.putLong(subscriptionId.longValue());
        // Put the interval as an int (4 bytes)
        buffer.putInt(interval.intValue());

        // Calculate the keccak256 hash of the packed data
        return Hash.sha3(buffer.array());
    }

    public CompletableFuture<Boolean> hasRequestCommitments(long subscriptionId, long interval) {
        byte[] requestId = getRequestId(BigInteger.valueOf(subscriptionId), BigInteger.valueOf(interval));
        return web3DelegateeCoordinatorService
            .getRequestCommitment(requestId)
            .thenApply(commitment -> {
                // A commitment of bytes32(0) is a 32-byte array filled with zeros.
                // We check if the returned commitment is NOT equal to that.
                return !Arrays.equals(commitment, new byte[32]);
            });
    }
}

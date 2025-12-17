package io.hpp.noosphere.agent.service.dto;

import static io.hpp.noosphere.agent.config.Constants.ZERO_ADDRESS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hpp.noosphere.agent.contracts.DelegateeCoordinator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.*;
import org.springframework.cache.annotation.Cacheable;
import org.web3j.crypto.StructuredData;
import org.web3j.crypto.StructuredData.Entry;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

/**
 * Noosphere 구독 정보를 전송하기 위한 DTO
 * Solidity Subscription struct 기반
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "id", "client" })
public class SubscriptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 데이터베이스 ID (Entity에서만 사용)
     */
    private Long id;

    /**
     * 컨테이너 식별자 (bytes32)
     * 컨테이너 이름 목록의 해시로 표현
     */
    @NotNull(message = "Route ID is required")
    @Pattern(regexp = "^0x[a-fA-F0-9]{64}$", message = "Invalid bytes32 format for routeId")
    private String routeId;

    /**
     * 컨테이너 식별자 (bytes32)
     * 컨테이너 이름 목록의 해시로 표현
     */
    @NotNull(message = "Container ID is required")
    @Pattern(regexp = "^0x[a-fA-F0-9]{64}$", message = "Invalid bytes32 format for containerId")
    private String containerId;

    /**
     * 구독 처리 시마다 지불할 금액
     * 0이면 지불 없음
     */
    @Min(value = 0, message = "Payment amount must be non-negative")
    @Builder.Default
    private BigInteger feeAmount = BigInteger.ZERO;

    /**
     * 구독 소유자 및 수신자 주소
     * BaseConsumer를 상속해야 하는 Ethereum 주소
     */
    @NotNull(message = "Owner address is required")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format")
    private String client;

    /**
     * 구독이 처음 활성화되는 타임스탬프 (Unix timestamp)
     * null이면 즉시 활성화
     */
    @Min(value = 0, message = "ActiveAt must be positive")
    @Max(value = 4_294_967_295L, message = "ActiveAt exceeds uint32 maximum")
    private Long activeAt;

    /**
     * 각 구독 간격 사이의 시간(초)
     * 0이면 즉시 활성화
     */
    @Min(value = 0, message = "Period must be non-negative")
    @Max(value = 4_294_967_295L, message = "Period exceeds uint32 maximum")
    @Builder.Default
    private Long intervalSeconds = 0L;

    /**
     * 구독이 처리되는 횟수
     * 1: 일회성 구독, >1: 반복 구독
     */
    @NotNull(message = "Frequency is required")
    @Min(value = 1, message = "Frequency must be at least 1")
    @Max(value = 4_294_967_295L, message = "Frequency exceeds uint32 maximum")
    @Builder.Default
    private Long maxExecutions = 1L;

    /**
     * 지불용 지갑 주소
     * owner가 승인된 소비자여야 함
     */
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format for wallet")
    private String wallet;

    /**
     * 지불 토큰 주소
     * null 또는 0x0...0이면 Ether 사용
     */
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format for paymentToken")
    private String feeToken;

    /**
     * 검증자 계약 주소
     * null 또는 0x0...0이면 검증 불필요
     */
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format for verifier")
    private String verifier;

    /**
     * 각 간격에서 구독을 이행할 수 있는 고유 노드 수
     */
    @Min(value = 0, message = "Redundancy must be non-negative")
    @Max(value = 65_535, message = "Redundancy exceeds uint16 maximum")
    @Builder.Default
    private Integer redundancy = 1;

    /**
     * 지연 저장 여부
     * true: Inbox에 저장, false: 즉시 전달
     */
    @Builder.Default
    private Boolean useDeliveryInbox = false;

    /**
     * Tracks the number of responses for a given interval.
     * Key: Interval number, Value: Response count.
     * Using ConcurrentHashMap for thread-safety.
     */
    private final Map<Long, Long> responses = new ConcurrentHashMap<>();

    /**
     * Tracks if the local node has already replied for a given interval.
     * Key: Interval number, Value: true if replied, false otherwise.
     * Using ConcurrentHashMap for thread-safety.
     */
    private final Map<Long, Boolean> nodeReplied = new ConcurrentHashMap<>();

    // === 편의 메서드들 ===

    /**
     * 일회성 구독인지 확인
     */
    public boolean isOneTimeSubscription() {
        return maxExecutions != null && maxExecutions == 1L;
    }

    /**
     * 반복 구독인지 확인
     */
    public boolean isRecurringSubscription() {
        return maxExecutions != null && maxExecutions > 1L;
    }

    /**
     * 즉시 활성화되는지 확인
     */
    public boolean isImmediatelyActive() {
        return intervalSeconds == null || intervalSeconds == 0L;
    }

    /**
     * 지연 구독인지 확인
     */
    public boolean useDeliveryInbox() {
        return useDeliveryInbox != null && useDeliveryInbox;
    }

    /**
     * 콜백 구독인지 확인 (간격이 0인 경우)
     */
    public boolean isCallback() {
        return intervalSeconds != null && intervalSeconds == 0L;
    }

    /**
     * 지불이 있는 구독인지 확인
     */
    public boolean hasPayment() {
        return feeAmount != null && feeAmount.compareTo(BigInteger.ZERO) > 0;
    }

    /**
     * 검증자가 있는지 확인
     */
    public boolean hasVerifier() {
        return verifier != null && !verifier.equals(ZERO_ADDRESS) && !verifier.isEmpty();
    }

    /**
     *  Check if subscription is cancelled
     */
    public boolean isCancelled() {
        return client == null || client.isEmpty() || client.equals(ZERO_ADDRESS);
    }

    /**
     * 커스텀 toString (긴 주소들을 축약해서 표시)
     */
    @Override
    public String toString() {
        return (
            "SubscriptionDTO{" +
            "id=" +
            id +
            ", client='" +
            abbreviateAddress(client) +
            '\'' +
            ", maxExecutions=" +
            maxExecutions +
            ", containerId='" +
            abbreviateHash(containerId) +
            '\'' +
            ", useDeliveryInbox=" +
            useDeliveryInbox +
            '}'
        );
    }

    private String abbreviateAddress(String address) {
        if (address == null || address.length() < 10) {
            return address;
        }
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }

    private String abbreviateHash(String hash) {
        if (hash == null || hash.length() < 16) {
            return hash;
        }
        return hash.substring(0, 10) + "..." + hash.substring(hash.length() - 6);
    }

    public boolean isActive() {
        return Instant.now().getEpochSecond() > this.activeAt;
    }

    public long getInterval() {
        if (this.intervalSeconds == 0) {
            return 1; // Callback subscriptions are always in interval 1
        }

        long now = Instant.now().getEpochSecond();
        return (((now - this.activeAt) / this.intervalSeconds) + 1);
    }

    /**
     * Returns the number of responses tracked for a given interval.
     *
     * @param interval The subscription interval.
     * @return The number of responses, defaults to 0 if not tracked.
     */
    public long getResponseCount(Long interval) {
        return this.responses.getOrDefault(interval, 0L);
    }

    /**
     * Sets the response count for a subscription interval.
     *
     * @param interval The subscription interval to set.
     * @param count    The count of tracked subscription responses.
     */
    public void setResponseCount(Long interval, long count) {
        this.responses.put(interval, count);
    }

    /**
     * Checks if the local node has replied in the given interval.
     *
     * @param interval The subscription interval to check.
     * @return true if the node has replied, false otherwise.
     */
    public boolean hasNodeReplied(Long interval) {
        return this.nodeReplied.getOrDefault(interval, false);
    }

    /**
     * Sets the local node as having responded for the given interval.
     *
     * @param interval The subscription interval.
     */
    public void setNodeReplied(Long interval) {
        this.nodeReplied.put(interval, true);
    }

    public boolean isCompleted() {
        boolean isLastInterval = (isPastLastInterval() || isOnLastInterval());
        boolean maxRedundancyMet = getResponseCount(this.maxExecutions) >= this.redundancy;
        return isLastInterval && maxRedundancyMet;
    }

    public boolean isPastLastInterval() {
        if (!isActive()) {
            return false;
        }
        return getInterval() > this.maxExecutions;
    }

    public boolean isOnLastInterval() {
        if (!isActive()) {
            return false;
        }
        return getInterval() == this.maxExecutions;
    }

    /**
     * Generates the EIP-712 typed data hash for a DelegateSubscription to be signed.
     * The result of this method is cached to avoid re-computation for the same inputs.
     *
     * @param nonce The delegatee signer nonce (relative to the owner contract).
     * @param expiry The signature expiry timestamp.
     * @param chainId The contract chain ID (for replay protection).
     * @param verifyingContract The EIP-712 signature verifying contract address.
     * @return The EIP-712 message hash to be signed.
     */
    public byte[] getTypedDataHashForDelegation(long nonce, long expiry, BigInteger chainId, String verifyingContract) {
        // Define the EIP-712 types
        HashMap<String, List<Entry>> types = new HashMap<>();
        types.put(
            "EIP712Domain",
            Arrays.asList(
                new Entry("name", "string"),
                new Entry("version", "string"),
                new Entry("chainId", "uint256"),
                new Entry("verifyingContract", "address")
            )
        );
        types.put(
            "DelegateSubscription",
            Arrays.asList(new Entry("nonce", "uint256"), new Entry("expiry", "uint256"), new Entry("sub", "Subscription"))
        );
        types.put(
            "Subscription",
            Arrays.asList(
                new Entry("client", "address"),
                new Entry("activeAt", "uint256"),
                new Entry("intervalSeconds", "uint256"),
                new Entry("maxExecutions", "uint256"),
                new Entry("redundancy", "uint256"),
                new Entry("containerId", "bytes32"),
                new Entry("useDeliveryInbox", "bool"),
                new Entry("verifier", "address"),
                new Entry("feeAmount", "uint256"),
                new Entry("feeToken", "address"),
                new Entry("wallet", "address"),
                new Entry("routeId", "bytes32")
            )
        );

        // Define the domain separator
        // The salt is optional and not needed in most cases. A zero-bytes32 value is standard.
        String salt = "0x0000000000000000000000000000000000000000000000000000000000000000";
        StructuredData.EIP712Domain domain = new StructuredData.EIP712Domain("noosphere", "1", chainId.toString(), verifyingContract, salt);

        // Define the message content
        Map<String, Object> subscriptionMessage = new HashMap<>();
        subscriptionMessage.put("client", this.client);
        subscriptionMessage.put("activeAt", this.activeAt);
        subscriptionMessage.put("intervalSeconds", this.intervalSeconds);
        subscriptionMessage.put("maxExecutions", this.maxExecutions);
        subscriptionMessage.put("redundancy", this.redundancy);
        subscriptionMessage.put("containerId", Numeric.hexStringToByteArray(this.getContainerId()));
        subscriptionMessage.put("useDeliveryInbox", this.useDeliveryInbox);
        subscriptionMessage.put("verifier", this.verifier);
        subscriptionMessage.put("feeAmount", this.feeAmount);
        subscriptionMessage.put("feeToken", this.feeToken);
        subscriptionMessage.put("wallet", this.wallet);
        subscriptionMessage.put("routeId", this.routeId);

        Map<String, Object> delegateMessage = new HashMap<>();
        delegateMessage.put("nonce", nonce);
        delegateMessage.put("expiry", expiry);
        delegateMessage.put("sub", subscriptionMessage);

        // Create the TypedData object
        StructuredData.EIP712Message typedData = new StructuredData.EIP712Message(
            types,
            "DelegateSubscription", // Primary Type
            delegateMessage,
            domain
        );

        // Return the hash of the structured data
        return new StructuredDataEncoder(typedData).hashStructuredData();
    }

    /**
     * Converts this DTO to the Web3j-generated ComputeSubscription struct for the DelegateeCoordinator contract.
     * @return A {@link DelegateeCoordinator.ComputeSubscription} instance.
     */
    @JsonIgnore
    public DelegateeCoordinator.ComputeSubscription toCoordinatorComputeSubscription() {
        return new DelegateeCoordinator.ComputeSubscription(
            Numeric.hexStringToByteArray(this.getRouteId()),
            Numeric.hexStringToByteArray(this.getContainerId()),
            this.getFeeAmount(),
            this.getClient(),
            BigInteger.valueOf(this.getActiveAt()),
            BigInteger.valueOf(this.getIntervalSeconds()),
            BigInteger.valueOf(this.getMaxExecutions()),
            this.getWallet(),
            this.getFeeToken(),
            this.getVerifier(),
            BigInteger.valueOf(this.getRedundancy()),
            this.getUseDeliveryInbox()
        );
    }
}

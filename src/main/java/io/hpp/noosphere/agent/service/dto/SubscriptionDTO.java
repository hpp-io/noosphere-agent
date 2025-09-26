package io.hpp.noosphere.agent.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;

/**
 * Noosphere 구독 정보를 전송하기 위한 DTO
 * Solidity Subscription struct 기반
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "id", "owner" })
public class SubscriptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 데이터베이스 ID (Entity에서만 사용)
     */
    private Long id;

    /**
     * 구독 소유자 및 수신자 주소
     * BaseConsumer를 상속해야 하는 Ethereum 주소
     */
    @NotNull(message = "Owner address is required")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format")
    private String owner;

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
    private Long period = 0L;

    /**
     * 구독이 처리되는 횟수
     * 1: 일회성 구독, >1: 반복 구독
     */
    @NotNull(message = "Frequency is required")
    @Min(value = 1, message = "Frequency must be at least 1")
    @Max(value = 4_294_967_295L, message = "Frequency exceeds uint32 maximum")
    @Builder.Default
    private Long frequency = 1L;

    /**
     * 각 간격에서 구독을 이행할 수 있는 고유 노드 수
     */
    @Min(value = 0, message = "Redundancy must be non-negative")
    @Max(value = 65_535, message = "Redundancy exceeds uint16 maximum")
    @Builder.Default
    private Integer redundancy = 1;

    /**
     * 컨테이너 식별자 (bytes32)
     * 컨테이너 이름 목록의 해시로 표현
     */
    @NotNull(message = "Container ID is required")
    @Pattern(regexp = "^0x[a-fA-F0-9]{64}$", message = "Invalid bytes32 format for containerId")
    private String containerId;

    /**
     * 지연 저장 여부
     * true: Inbox에 저장, false: 즉시 전달
     */
    @Builder.Default
    private Boolean lazy = false;

    /**
     * 검증자 계약 주소
     * null 또는 0x0...0이면 검증 불필요
     */
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format for verifier")
    private String verifier;

    /**
     * 구독 처리 시마다 지불할 금액
     * 0이면 지불 없음
     */
    @Min(value = 0, message = "Payment amount must be non-negative")
    @Builder.Default
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    /**
     * 지불 토큰 주소
     * null 또는 0x0...0이면 Ether 사용
     */
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format for paymentToken")
    private String paymentToken;

    /**
     * 지불용 지갑 주소
     * owner가 승인된 소비자여야 함
     */
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format for wallet")
    private String wallet;

    // === 편의 메서드들 ===

    /**
     * 일회성 구독인지 확인
     */
    public boolean isOneTimeSubscription() {
        return frequency != null && frequency == 1L;
    }

    /**
     * 반복 구독인지 확인
     */
    public boolean isRecurringSubscription() {
        return frequency != null && frequency > 1L;
    }

    /**
     * 즉시 활성화되는지 확인
     */
    public boolean isImmediatelyActive() {
        return period == null || period == 0L;
    }

    /**
     * 지연 구독인지 확인
     */
    public boolean isLazySubscription() {
        return lazy != null && lazy;
    }

    /**
     * 지불이 있는 구독인지 확인
     */
    public boolean hasPayment() {
        return paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 검증자가 있는지 확인
     */
    public boolean hasVerifier() {
        return verifier != null && !verifier.equals("0x0000000000000000000000000000000000000000") && !verifier.isEmpty();
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
            ", owner='" +
            abbreviateAddress(owner) +
            '\'' +
            ", frequency=" +
            frequency +
            ", containerId='" +
            abbreviateHash(containerId) +
            '\'' +
            ", lazy=" +
            lazy +
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
}

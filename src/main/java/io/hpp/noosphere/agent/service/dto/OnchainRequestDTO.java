package io.hpp.noosphere.agent.service.dto;

import io.hpp.noosphere.agent.contracts.Router;
import io.hpp.noosphere.agent.service.dto.enumeration.RequestType;
import java.util.Optional;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OnchainRequestDTO extends BaseRequestDTO {

    private SubscriptionDTO subscription;
    private Router.Commitment commitment;

    @Builder.Default
    private RequestType type = RequestType.ON_CHAIN_COMPUTATION;

    @Builder.Default
    private Optional<Boolean> requiresProof = Optional.of(false);

    /**
     * Creates an OnchainRequestDTO from a RequestStart event and the corresponding wallet address.
     * This method also constructs the Commitment object needed for the request.
     *
     * @param event The event response from the contract.
     * @param walletAddress The wallet address associated with the subscription.
     * @return A fully constructed OnchainRequestDTO.
     */
    public static OnchainRequestDTO fromEvent(Router.RequestStartEventResponse event, String walletAddress) {
        // Create the Commitment object from the event data and the wallet address
        Router.Commitment commitment = new Router.Commitment(
            event.requestId,
            event.subscriptionId,
            event.containerId,
            event.interval,
            event.useDeliveryInbox,
            event.redundancy,
            walletAddress, // The wallet address fetched separately
            event.feeAmount,
            event.feeToken,
            event.verifier,
            event.coordinator
        );

        return OnchainRequestDTO.builder().commitment(commitment).build();
    }
}

package io.hpp.noosphere.agent.service.dto;

import io.hpp.noosphere.agent.service.blockchain.dto.SignatureParamsDTO;
import io.hpp.noosphere.agent.service.dto.enumeration.RequestType;
import java.util.Map;
import java.util.Optional;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DelegatedRequestDTO extends BaseRequestDTO {

    private SubscriptionDTO subscription;
    private SignatureParamsDTO signature;
    private Map<String, Object> data;

    @Builder.Default
    private RequestType type = RequestType.DELEGATED_COMPUTATION;

    @Builder.Default
    private Optional<Boolean> requiresProof = Optional.of(false);
}

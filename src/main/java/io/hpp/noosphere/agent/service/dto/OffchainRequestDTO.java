package io.hpp.noosphere.agent.service.dto;

import io.hpp.noosphere.agent.service.dto.enumeration.RequestType;
import java.util.List;
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
public class OffchainRequestDTO extends BaseRequestDTO {

    private List<String> containers;
    private Map<String, Object> data;

    @Builder.Default
    private RequestType type = RequestType.OFF_CHAIN_COMPUTATION;

    @Builder.Default
    private Boolean requiresProof = false;
}

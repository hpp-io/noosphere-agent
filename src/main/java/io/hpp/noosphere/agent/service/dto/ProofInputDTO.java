package io.hpp.noosphere.agent.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProofInputDTO {

    private String requestId;
    private InlineDataDTO commitment;
    private InlineDataDTO delegatedSubscription;
    private InlineDataDTO input;
    private InlineDataDTO output;
}

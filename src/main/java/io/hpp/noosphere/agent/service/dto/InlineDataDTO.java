package io.hpp.noosphere.agent.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InlineDataDTO {

    @Builder.Default
    private String type = "inline";

    private String value; // Hex string
}

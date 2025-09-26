package io.hpp.noosphere.agent.service.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContainerInputDTO {

    private String source;
    private String destination;
    private Map<String, Object> data;
    private boolean requiresProof;
}

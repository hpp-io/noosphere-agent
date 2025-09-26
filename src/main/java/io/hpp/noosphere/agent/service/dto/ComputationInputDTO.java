package io.hpp.noosphere.agent.service.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComputationInputDTO {

    private String source;
    private String destination;
    private Map<String, Object> data;
}

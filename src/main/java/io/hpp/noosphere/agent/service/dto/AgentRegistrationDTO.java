package io.hpp.noosphere.agent.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentRegistrationDTO {

    private String name;
    private String apiKey;
    private String walletAddress;
    private String email;
}

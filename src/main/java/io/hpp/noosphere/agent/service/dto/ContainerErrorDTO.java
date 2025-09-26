package io.hpp.noosphere.agent.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContainerErrorDTO implements ContainerResultDTO {

    private String container;
    private String error;
}

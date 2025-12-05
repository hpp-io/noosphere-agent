package io.hpp.noosphere.agent.service.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContainerOutputDTO implements ContainerResultDTO {

    private String container;
    private Object output;
    private String proof;
}

package io.hpp.noosphere.agent.service.dto;

import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode
public abstract class BaseRequestDTO {

    private UUID id;
    private String clientIp;
}

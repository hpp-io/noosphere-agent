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

    private UUID id; // 모든 요청이 공통으로 가질 수 있는 필드 예시
    private String clientIp;
}

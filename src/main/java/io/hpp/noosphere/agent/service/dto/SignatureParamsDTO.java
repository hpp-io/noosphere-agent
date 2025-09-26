package io.hpp.noosphere.agent.service.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SignatureParamsDTO {

    private int nonce;
    private int expiry;
    private int v;
    private int r;
    private int s;
}

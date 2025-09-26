package io.hpp.noosphere.agent.web.rest.vm;

import io.hpp.noosphere.agent.service.dto.BaseRequestDTO;
import io.hpp.noosphere.agent.service.dto.DelegatedRequestDTO;
import io.hpp.noosphere.agent.service.dto.OffchainRequestDTO;
import io.hpp.noosphere.agent.service.dto.enumeration.RequestType;
import lombok.Data;

@Data
public class RequestUnionVM {

    private RequestType requestType;
    private OffchainRequestDTO offchainRequest;
    private DelegatedRequestDTO delegatedRequest;

    // 편의 메서드
    public boolean isOffchainRequest() {
        return requestType == RequestType.OFF_CHAIN_COMPUTATION && offchainRequest != null;
    }

    public boolean isDelegatedRequest() {
        return requestType == RequestType.DELEGATED_COMPUTATION && delegatedRequest != null;
    }

    // 실제 요청 데이터 반환
    public BaseRequestDTO getActualRequest() {
        if (isOffchainRequest()) {
            return offchainRequest;
        } else if (isDelegatedRequest()) {
            return delegatedRequest;
        }
        return null;
    }

    // Validation
    public void validate() {
        if (requestType == null) {
            throw new IllegalArgumentException("Request type is required");
        }

        if (isOffchainRequest() && offchainRequest == null) {
            throw new IllegalArgumentException("Offchain request data is required for OFF_CHAIN_COMPUTATION type");
        }

        if (isDelegatedRequest() && delegatedRequest == null) {
            throw new IllegalArgumentException("Delegated request data is required for DELEGATED type");
        }

        // 둘 다 설정되면 안됨
        if (offchainRequest != null && delegatedRequest != null) {
            throw new IllegalArgumentException("Only one request type should be provided");
        }
    }
}

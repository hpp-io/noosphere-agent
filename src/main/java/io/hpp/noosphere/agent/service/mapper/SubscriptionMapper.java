package io.hpp.noosphere.agent.service.mapper;

import io.hpp.noosphere.agent.contracts.Router;
import io.hpp.noosphere.agent.contracts.SubscriptionBatchReader;
import io.hpp.noosphere.agent.service.ContainerLookupService;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import java.nio.charset.StandardCharsets;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    default SubscriptionDTO toDto(
        long currentId,
        Router.ComputeSubscription contractSub,
        @Context ContainerLookupService containerLookupService
    ) {
        if (contractSub == null) {
            return null;
        }

        return SubscriptionDTO.builder()
            .id(currentId)
            .routeId(new String(contractSub.routeId, StandardCharsets.UTF_8).replaceAll("\\x00+$", ""))
            .containerId(containerLookupService.getContainersHashString(contractSub.containerId))
            .feeAmount(contractSub.feeAmount)
            .feeToken(contractSub.feeToken)
            .client(contractSub.client)
            .activeAt(contractSub.activeAt.longValue())
            .intervalSeconds(contractSub.intervalSeconds.longValue())
            .maxExecutions(contractSub.maxExecutions.longValue())
            .wallet(contractSub.wallet)
            .verifier(contractSub.verifier)
            .redundancy(contractSub.redundancy.intValue())
            .useDeliveryInbox(contractSub.useDeliveryInbox)
            .build();
    }

    default SubscriptionDTO toDto(
        long currentId,
        SubscriptionBatchReader.ComputeSubscription contractSub,
        @Context ContainerLookupService containerLookupService
    ) {
        if (contractSub == null) {
            return null;
        }

        return SubscriptionDTO.builder()
            .id(currentId)
            .routeId(new String(contractSub.routeId, StandardCharsets.UTF_8).replaceAll("\\x00+$", ""))
            .containerId(containerLookupService.getContainersHashString(contractSub.containerId))
            .feeAmount(contractSub.feeAmount)
            .feeToken(contractSub.feeToken)
            .client(contractSub.client)
            .activeAt(contractSub.activeAt.longValue())
            .intervalSeconds(contractSub.intervalSeconds.longValue())
            .maxExecutions(contractSub.maxExecutions.longValue())
            .wallet(contractSub.wallet)
            .verifier(contractSub.verifier)
            .redundancy(contractSub.redundancy.intValue())
            .useDeliveryInbox(contractSub.useDeliveryInbox)
            .build();
    }
}

package io.hpp.noosphere.agent.service.mapper;

import io.hpp.noosphere.agent.contracts.Router;
import io.hpp.noosphere.agent.contracts.SubscriptionBatchReader;
import io.hpp.noosphere.agent.service.ContainerLookupService;
import io.hpp.noosphere.agent.service.dto.SubscriptionDTO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(target = "id", source = "currentId")
    @Mapping(
        target = "routeId",
        expression = "java(new String(contractSub.routeId, java.nio.charset.StandardCharsets.UTF_8).replaceAll(\"\\\\x00+$\", \"\"))"
    )
    @Mapping(target = "containerId", expression = "java(containerLookupService.getContainersHashString(contractSub.containerId))")
    @Mapping(target = "activeAt", expression = "java(contractSub.activeAt.longValue())")
    @Mapping(target = "intervalSeconds", expression = "java(contractSub.intervalSeconds.longValue())")
    @Mapping(target = "maxExecutions", expression = "java(contractSub.maxExecutions.longValue())")
    @Mapping(target = "redundancy", expression = "java(contractSub.redundancy.intValue())")
    SubscriptionDTO toDto(
        long currentId,
        SubscriptionBatchReader.ComputeSubscription contractSub,
        @Context ContainerLookupService containerLookupService
    );

    @Mapping(target = "id", source = "currentId")
    @Mapping(
        target = "routeId",
        expression = "java(new String(contractSub.routeId, java.nio.charset.StandardCharsets.UTF_8).replaceAll(\"\\\\x00+$\", \"\"))"
    )
    @Mapping(target = "containerId", expression = "java(containerLookupService.getContainersHashString(contractSub.containerId))")
    @Mapping(target = "activeAt", expression = "java(contractSub.activeAt.longValue())")
    @Mapping(target = "intervalSeconds", expression = "java(contractSub.intervalSeconds.longValue())")
    @Mapping(target = "maxExecutions", expression = "java(contractSub.maxExecutions.longValue())")
    @Mapping(target = "redundancy", expression = "java(contractSub.redundancy.intValue())")
    SubscriptionDTO toDto(long currentId, Router.ComputeSubscription contractSub, @Context ContainerLookupService containerLookupService);
}

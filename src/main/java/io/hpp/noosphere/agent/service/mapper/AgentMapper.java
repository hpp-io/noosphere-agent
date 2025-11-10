package io.hpp.noosphere.agent.service.mapper;

import io.hpp.noosphere.agent.domain.Agent;
import io.hpp.noosphere.agent.service.dto.AgentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link Agent} and its DTO {@link AgentDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface AgentMapper extends EntityMapper<AgentDTO, Agent> {
    AgentDTO toDto(Agent s);

    Agent toEntity(AgentDTO s);
}

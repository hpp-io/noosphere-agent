package io.hpp.noosphere.agent.service;

import io.hpp.noosphere.agent.domain.Agent;
import io.hpp.noosphere.agent.repository.AgentRepository;
import io.hpp.noosphere.agent.service.dto.AgentDTO;
import io.hpp.noosphere.agent.service.mapper.AgentMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;

    @Getter
    @Setter
    private AgentDTO registeredAgent;

    public AgentService(AgentRepository agentRepository, AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    /**
     * Starts the hub registration process once the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void getRegisteredAgentInfo() {
        List<AgentDTO> agentList = this.getAll();
        if (agentList.size() > 1) {
            throw new IllegalStateException("There should be only one registered agent in the database.");
        } else if (agentList.isEmpty()) {
            this.registeredAgent = null;
        } else {
            this.registeredAgent = agentList.get(0);
        }
    }

    @Transactional(readOnly = true)
    public Optional<AgentDTO> get(UUID agentId) {
        return agentRepository.findById(agentId).map(agentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AgentDTO> getAll() {
        return agentRepository.findAll().stream().map(agentMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public AgentDTO save(AgentDTO agentDTO) {
        Agent agent = agentMapper.toEntity(agentDTO);
        return agentMapper.toDto(agentRepository.save(agent));
    }

    @Transactional
    public void syncAgent(AgentDTO agentDTO) {
        if (!this.getAll().isEmpty()) {
            agentRepository.deleteAll();
        }
        Agent agent = agentMapper.toEntity(agentDTO);
        agentMapper.toDto(agentRepository.save(agent));
        this.registeredAgent = agentDTO;
    }
}

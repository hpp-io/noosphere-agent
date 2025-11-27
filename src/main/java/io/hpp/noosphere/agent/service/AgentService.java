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
    private volatile AgentDTO registeredAgent;

    public AgentService(AgentRepository agentRepository, AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    @Transactional(readOnly = true)
    public Optional<AgentDTO> get(UUID agentId) {
        return agentRepository.findById(agentId).map(agentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AgentDTO> getAll() {
        return agentRepository.findAll().stream().map(agentMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AgentDTO> findFirst() {
        return agentRepository.findFirstByOrderByCreatedAtDesc().map(agentMapper::toDto);
    }

    @Transactional
    public AgentDTO save(AgentDTO agentDTO) {
        log.debug("Saving agent DTO: {}", agentDTO);

        Agent agent = agentMapper.toEntity(agentDTO);
        log.debug("Mapped to entity: {}", agent);

        Agent savedAgent = agentRepository.save(agent);
        log.debug("Saved agent entity: {}", savedAgent);

        AgentDTO result = agentMapper.toDto(savedAgent);
        log.debug("Mapped back to DTO: {}", result);

        return result;
    }

    @Transactional
    public void syncAgent(AgentDTO agentDTO) {
        if (agentDTO == null || agentDTO.getId() == null) {
            log.error("Cannot sync agent with null DTO or ID.");
            return;
        }

        try {
            Optional<Agent> existingAgent = agentRepository.findById(agentDTO.getId());
            Agent agentToSave;

            if (existingAgent.isPresent()) {
                // 기존 엔티티를 직접 수정 (같은 영속성 컨텍스트에서)
                log.info("Updating existing agent with ID: {}", agentDTO.getId());
                agentToSave = existingAgent.get();
                agentMapper.partialUpdate(agentToSave, agentDTO);
            } else {
                // 새 엔티티 생성
                log.info("Creating new agent with ID: {}", agentDTO.getId());
                agentToSave = agentMapper.toEntity(agentDTO);
                // Manually set the ID as it's not generated and might not be mapped correctly.
                agentToSave.setId(agentDTO.getId());
            }

            // 직접 repository.save() 호출 (save() 메소드를 거치지 않음)
            Agent savedAgent = agentRepository.save(agentToSave);
            this.registeredAgent = agentMapper.toDto(savedAgent);
            log.info("Agent {} synced successfully.", savedAgent.getId());
        } catch (Exception e) {
            log.error("Failed to sync agent {}: {}", agentDTO.getId(), e.getMessage(), e);
            throw e;
        }
    }
}

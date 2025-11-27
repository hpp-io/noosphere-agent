package io.hpp.noosphere.agent.repository;

import io.hpp.noosphere.agent.domain.Agent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepository extends JpaRepository<Agent, UUID> {
    Optional<Agent> findFirstByOrderByCreatedAtDesc();
}

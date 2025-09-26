package io.hpp.noosphere.agent.repository;

import io.hpp.noosphere.agent.domain.ComputationCounter;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputationCounterRepository extends JpaRepository<ComputationCounter, ComputationCounter.ComputationCounterId> {
    Optional<ComputationCounter> findByStatus(ComputationStatus status);

    Optional<ComputationCounter> findByLocationAndStatus(ComputationLocation location, ComputationStatus status);

    @Modifying
    @Query("UPDATE ComputationCounter c SET c.count = c.count + 1 WHERE c.location =  :location AND c.status = :status")
    int incrementCounter(@Param("location") ComputationLocation location, @Param("status") ComputationStatus status);

    @Modifying
    @Query("UPDATE ComputationCounter c SET c.count = 0")
    int resetAllCounters();
}

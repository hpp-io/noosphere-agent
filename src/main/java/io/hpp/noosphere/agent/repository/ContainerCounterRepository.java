package io.hpp.noosphere.agent.repository;

import io.hpp.noosphere.agent.domain.ContainerCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContainerCounterRepository extends JpaRepository<ContainerCounter, String> {
    @Modifying
    @Query("UPDATE ContainerCounter cc SET cc.successCount = cc.successCount + 1 WHERE cc.containerId = :containerId")
    int incrementSuccess(@Param("containerId") String containerId);

    @Modifying
    @Query("UPDATE ContainerCounter cc SET cc.failedCount = cc.failedCount + 1 WHERE cc.containerId = :containerId")
    int incrementFailure(@Param("containerId") String containerId);

    @Modifying
    @Query("UPDATE ContainerCounter cc SET cc.successCount = 0, cc.failedCount = 0")
    void resetAllCounters();
}

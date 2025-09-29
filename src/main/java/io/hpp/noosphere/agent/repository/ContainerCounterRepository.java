package io.hpp.noosphere.agent.repository;

import io.hpp.noosphere.agent.domain.ContainerCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContainerCounterRepository extends JpaRepository<ContainerCounter, String> {
    @Modifying
    @Query(
        "UPDATE ContainerCounter cc SET cc.successCount = cc.successCount + 1, cc.lastUpdated = CURRENT_TIMESTAMP WHERE cc.containerId = :containerId"
    )
    int incrementSuccess(@Param("containerId") String containerId);

    @Modifying
    @Query(
        "UPDATE ContainerCounter cc SET cc.failedCount = cc.failedCount + 1, cc.lastUpdated = CURRENT_TIMESTAMP WHERE cc.containerId = :containerId"
    )
    int incrementFailure(@Param("containerId") String containerId);

    @Modifying
    @Query("UPDATE ContainerCounter cc SET cc.successCount = 0, cc.failedCount = 0")
    void resetAllCounters();

    // PostgreSQL UPSERT 쿼리 추가
    @Modifying
    @Query(
        value = """
        INSERT INTO container_counter (container_id, success_count, failed_count, last_updated)
        VALUES (:containerId, :successCount, :failedCount, CURRENT_TIMESTAMP)
        ON CONFLICT (container_id)
        DO UPDATE SET
            success_count = container_counter.success_count + :successCount,
            failed_count = container_counter.failed_count + :failedCount,
            last_updated = CURRENT_TIMESTAMP
        """,
        nativeQuery = true
    )
    void upsertCounter(
        @Param("containerId") String containerId,
        @Param("successCount") long successCount,
        @Param("failedCount") long failedCount
    );
}

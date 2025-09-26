package io.hpp.noosphere.agent.repository;

import io.hpp.noosphere.agent.domain.Computation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputationRepository extends JpaRepository<Computation, UUID> {
    List<Computation> findByStatus(String status);

    List<Computation> findByClientIp(String clientIp);

    List<Computation> findByClientIpAndStatus(String clientIp, String status);

    Optional<Computation> findByIdAndClientIp(UUID id, String clientIp);

    @Query("SELECT c FROM Computation c WHERE c.status = 'running' AND c.createdAt < :expiredTime")
    List<Computation> findExpiredRunningComputations(@Param("expiredTime") LocalDateTime expiredTime);

    @Query("SELECT COUNT(c) FROM Computation c WHERE c.status = :status")
    Long countByStatus(@Param("status") String status);

    void deleteByCompletedAtBefore(LocalDateTime expiredTime);
}

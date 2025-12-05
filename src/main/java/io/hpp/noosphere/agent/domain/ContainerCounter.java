package io.hpp.noosphere.agent.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "container_counter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerCounter implements Serializable {

    @Id
    @Column(name = "container_id")
    private String containerId;

    @Builder.Default
    @Column(name = "success_count", nullable = false)
    private Long successCount = 0L;

    @Builder.Default
    @Column(name = "failed_count", nullable = false)
    private Long failedCount = 0L;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}

package io.hpp.noosphere.agent.domain;

import jakarta.persistence.*;
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
public class ContainerCounter {

    @Id
    @Column(name = "container_id")
    private String containerId;

    @Column(name = "success_count", nullable = false)
    private Long successCount = 0L;

    @Column(name = "failed_count", nullable = false)
    private Long failedCount = 0L;
}

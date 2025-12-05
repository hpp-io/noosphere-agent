package io.hpp.noosphere.agent.domain;

import io.hpp.noosphere.agent.service.dto.enumeration.ComputationLocation;
import io.hpp.noosphere.agent.service.dto.enumeration.ComputationStatus;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "computation_counter")
@IdClass(ComputationCounter.ComputationCounterId.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@RequiredArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ComputationCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Enumerated(EnumType.STRING)
    private ComputationLocation location;

    @Id
    @Enumerated(EnumType.STRING)
    private ComputationStatus status; // running, success, failed

    @Builder.Default
    @Column(nullable = false)
    private Long count = 0L;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public static class ComputationCounterId implements Serializable {

        private ComputationLocation location;
        private ComputationStatus status;

        public ComputationCounterId() {}

        public ComputationCounterId(ComputationLocation location, ComputationStatus status) {
            this.location = location;
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComputationCounterId that = (ComputationCounterId) o;
            return location == that.location && status == that.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, status);
        }
    }
}

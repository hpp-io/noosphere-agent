package io.hpp.noosphere.agent.service.dto;

import io.hpp.noosphere.agent.service.dto.enumeration.StatusCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema
public class AgentDTO implements Serializable {

    private UUID id;

    private String name;

    private String walletAddress;

    private StatusCode statusCode;

    @Lob
    private String description;

    private Instant createdAt;

    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AgentDTO agentDTO)) {
            return false;
        }

        return new EqualsBuilder().append(id, agentDTO.id).append(name, agentDTO.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(name).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("walletAddress", walletAddress)
            .append("statusCode", statusCode)
            .append("description", description)
            .append("createdAt", createdAt)
            .append("updatedAt", updatedAt)
            .toString();
    }
}

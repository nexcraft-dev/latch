package dev.nexcraft.latch.controlplane.core.project.dto;

import dev.nexcraft.latch.controlplane.core.project.ProjectStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Project data model shared by the application and HTTP layers.
 *
 * @param id Project identifier
 * @param organizationId owning Organization identifier
 * @param name display name
 * @param key stable configuration key
 * @param description optional description
 * @param status Project lifecycle status
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record ProjectDetails(
        UUID id,
        UUID organizationId,
        String name,
        String key,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt) {
}

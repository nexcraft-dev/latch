package dev.nexcraft.latch.controlplane.core.organization.dto;

import dev.nexcraft.latch.controlplane.core.organization.OrganizationStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Organization data model shared by the application and HTTP layers.
 *
 * @param id organization identifier
 * @param name display name
 * @param slug immutable generated slug
 * @param status organization lifecycle status
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record OrganizationDetails(
        UUID id,
        String name,
        String slug,
        OrganizationStatus status,
        Instant createdAt,
        Instant updatedAt) {
}

package dev.nexcraft.latch.controlplane.core.project.command;

import java.util.UUID;

/**
 * Application input for creating a Project within an Organization.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param name display name
 * @param key stable Project key
 * @param description optional description
 */
public record CreateProjectCommand(
        UUID actorIdentityId,
        UUID organizationId,
        String name,
        String key,
        String description) {
}

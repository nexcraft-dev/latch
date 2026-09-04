package dev.nexcraft.latch.controlplane.core.project.command;

import java.util.UUID;

/**
 * Application input for changing mutable Project fields.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param projectId target Project
 * @param name replacement display name
 * @param description replacement description, or null to retain the existing value
 */
public record UpdateProjectCommand(
        UUID actorIdentityId,
        UUID organizationId,
        UUID projectId,
        String name,
        String description) {
}

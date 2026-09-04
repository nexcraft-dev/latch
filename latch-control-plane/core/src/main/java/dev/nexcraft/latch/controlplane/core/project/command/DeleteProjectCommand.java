package dev.nexcraft.latch.controlplane.core.project.command;

import java.util.UUID;

/**
 * Application input for soft-deleting a Project.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param projectId target Project
 */
public record DeleteProjectCommand(UUID actorIdentityId, UUID organizationId, UUID projectId) {
}

package dev.nexcraft.latch.controlplane.core.project.query;

import java.util.UUID;

/**
 * Application input for getting a Project through its Organization path.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId requested Organization
 * @param projectId requested Project
 */
public record GetProjectQuery(UUID actorIdentityId, UUID organizationId, UUID projectId) {
}

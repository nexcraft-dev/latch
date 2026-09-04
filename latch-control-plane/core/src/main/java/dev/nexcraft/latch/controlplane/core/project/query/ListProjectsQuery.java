package dev.nexcraft.latch.controlplane.core.project.query;

import java.util.UUID;

/**
 * Raw application input for listing active Projects.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param search optional search text
 * @param page requested zero-based page number
 * @param size requested page size
 * @param sort field and direction expression
 */
public record ListProjectsQuery(
        UUID actorIdentityId,
        UUID organizationId,
        String search,
        Integer page,
        Integer size,
        String sort) {
}

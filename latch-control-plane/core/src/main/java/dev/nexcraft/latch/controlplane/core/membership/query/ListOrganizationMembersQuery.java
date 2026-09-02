package dev.nexcraft.latch.controlplane.core.membership.query;

import java.util.UUID;

/**
 * Application input for listing active Organization members.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param page zero-based page number
 * @param size requested page size
 */
public record ListOrganizationMembersQuery(UUID actorIdentityId, UUID organizationId, Integer page, Integer size) {
}

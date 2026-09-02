package dev.nexcraft.latch.controlplane.core.membership.dto;

import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import java.time.Instant;
import java.util.UUID;

/**
 * Safe Organization membership data exposed by the HTTP layer.
 *
 * @param id membership identifier
 * @param organizationId Organization identifier
 * @param identityId member Identity identifier
 * @param email optional member email
 * @param displayName optional member display name
 * @param role Organization role
 * @param createdAt membership creation timestamp
 * @param updatedAt last role update timestamp
 */
public record OrganizationMember(
        UUID id,
        UUID organizationId,
        UUID identityId,
        String email,
        String displayName,
        MembershipRole role,
        Instant createdAt,
        Instant updatedAt) {
}

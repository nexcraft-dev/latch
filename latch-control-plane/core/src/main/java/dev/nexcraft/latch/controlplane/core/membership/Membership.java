package dev.nexcraft.latch.controlplane.core.membership;

import dev.nexcraft.latch.controlplane.core.membership.error.MembershipValidationException;
import java.time.Instant;
import java.util.UUID;

/**
 * Framework-free membership between an Identity and an Organization.
 *
 * @param id membership identifier
 * @param organizationId organization identifier
 * @param identityId member Identity identifier
 * @param role organization role
 * @param createdAt creation timestamp in UTC
 * @param updatedAt last role update timestamp in UTC
 */
public record Membership(
        UUID id,
        UUID organizationId,
        UUID identityId,
        MembershipRole role,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * Validates membership invariants.
     */
    public Membership {
        if (id == null) {
            throw new MembershipValidationException("Membership id is required");
        }
        if (organizationId == null) {
            throw new MembershipValidationException("Membership organization id is required");
        }
        if (identityId == null) {
            throw new MembershipValidationException("Membership identity id is required");
        }
        if (role == null) {
            throw new MembershipValidationException("Membership role is required");
        }
        if (createdAt == null || updatedAt == null) {
            throw new MembershipValidationException("Membership timestamps are required");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new MembershipValidationException("Membership updatedAt cannot be before createdAt");
        }
    }

    /**
     * Creates a membership.
     *
     * @param id membership identifier
     * @param organizationId organization identifier
     * @param identityId member Identity identifier
     * @param role organization role
     * @param now creation timestamp
     * @return new membership
     */
    public static Membership create(
            UUID id,
            UUID organizationId,
            UUID identityId,
            MembershipRole role,
            Instant now) {
        return new Membership(id, organizationId, identityId, role, now, now);
    }

    /**
     * Returns a membership with a changed role.
     *
     * @param replacement replacement role
     * @param now role update timestamp
     * @return updated membership
     */
    public Membership changeRole(MembershipRole replacement, Instant now) {
        return new Membership(id, organizationId, identityId, replacement, createdAt, now);
    }
}

package dev.nexcraft.latch.controlplane.core.membership.command;

import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import java.util.UUID;

/**
 * Application input for changing an Organization member role.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param membershipId membership to update
 * @param role replacement role
 */
public record UpdateOrganizationMemberCommand(
        UUID actorIdentityId,
        UUID organizationId,
        UUID membershipId,
        MembershipRole role) {
}

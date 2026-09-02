package dev.nexcraft.latch.controlplane.core.membership.command;

import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import java.util.UUID;

/**
 * Application input for adding an Organization member.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param identityId Identity to add
 * @param role requested role
 */
public record AddOrganizationMemberCommand(
        UUID actorIdentityId,
        UUID organizationId,
        UUID identityId,
        MembershipRole role) {
}

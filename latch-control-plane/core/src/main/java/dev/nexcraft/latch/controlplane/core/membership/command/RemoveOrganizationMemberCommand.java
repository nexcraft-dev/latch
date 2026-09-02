package dev.nexcraft.latch.controlplane.core.membership.command;

import java.util.UUID;

/**
 * Application input for removing an Organization member.
 *
 * @param actorIdentityId authenticated caller
 * @param organizationId target Organization
 * @param membershipId membership to remove
 */
public record RemoveOrganizationMemberCommand(UUID actorIdentityId, UUID organizationId, UUID membershipId) {
}

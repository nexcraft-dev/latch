package dev.nexcraft.latch.controlplane.core.membership.dto;

import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * HTTP input for adding an Organization member.
 *
 * @param identityId Identity to add
 * @param role requested Organization role
 */
public record AddOrganizationMember(@NotNull UUID identityId, @NotNull MembershipRole role) {
}

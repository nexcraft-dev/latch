package dev.nexcraft.latch.controlplane.core.membership.dto;

import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import jakarta.validation.constraints.NotNull;

/**
 * HTTP input for changing a membership role.
 *
 * @param role replacement Organization role
 */
public record UpdateMembershipRole(@NotNull MembershipRole role) {
}

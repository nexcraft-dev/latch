package dev.nexcraft.latch.controlplane.core.identity.dto;

import java.util.UUID;

/**
 * Safe current-Identity data exposed by the HTTP layer.
 *
 * @param id internal Identity identifier
 * @param email optional profile email
 * @param displayName optional profile display name
 */
public record IdentityDetails(UUID id, String email, String displayName) {
}

package dev.nexcraft.latch.controlplane.core.identity;

/**
 * Framework-free claims needed to resolve an externally authenticated Identity.
 *
 * @param provider verified external provider issuer
 * @param providerSubject verified external subject
 * @param email optional profile email
 * @param displayName optional profile display name
 */
public record IdentityClaims(String provider, String providerSubject, String email, String displayName) {
}

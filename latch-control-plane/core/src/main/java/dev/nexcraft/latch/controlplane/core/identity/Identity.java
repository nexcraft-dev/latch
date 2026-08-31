package dev.nexcraft.latch.controlplane.core.identity;

import dev.nexcraft.latch.controlplane.core.identity.error.IdentityValidationException;
import java.time.Instant;
import java.util.UUID;

/**
 * Framework-free representation of an externally authenticated Identity.
 *
 * @param id internal Identity identifier
 * @param provider verified external OIDC issuer
 * @param providerSubject verified OIDC subject
 * @param email optional profile email
 * @param displayName optional profile display name
 * @param createdAt creation timestamp in UTC
 * @param updatedAt last profile update timestamp in UTC
 */
public record Identity(
        UUID id,
        String provider,
        String providerSubject,
        String email,
        String displayName,
        Instant createdAt,
        Instant updatedAt) {

    /** Maximum provider identifier length. */
    public static final int MAX_PROVIDER_LENGTH = 100;

    /** Maximum external provider subject length. */
    public static final int MAX_PROVIDER_SUBJECT_LENGTH = 255;

    /** Maximum profile email length. */
    public static final int MAX_EMAIL_LENGTH = 320;

    /** Maximum display name length. */
    public static final int MAX_DISPLAY_NAME_LENGTH = 200;

    /**
     * Validates Identity invariants at the domain boundary.
     */
    public Identity {
        if (id == null) {
            throw new IdentityValidationException("Identity id is required");
        }
        provider = requireText(provider, "Identity provider", MAX_PROVIDER_LENGTH);
        providerSubject = requireText(providerSubject, "Identity provider subject", MAX_PROVIDER_SUBJECT_LENGTH);
        email = optionalText(email, "Identity email", MAX_EMAIL_LENGTH);
        displayName = optionalText(displayName, "Identity display name", MAX_DISPLAY_NAME_LENGTH);
        if (createdAt == null || updatedAt == null) {
            throw new IdentityValidationException("Identity timestamps are required");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IdentityValidationException("Identity updatedAt cannot be before createdAt");
        }
    }

    /**
     * Creates a new Identity from verified external claims.
     *
     * @param id internal Identity identifier
     * @param claims generic external identity claims
     * @param now creation timestamp
     * @return a new Identity
     */
    public static Identity create(UUID id, IdentityClaims claims, Instant now) {
        if (claims == null) {
            throw new IdentityValidationException("Identity claims are required");
        }
        return new Identity(
                id,
                claims.provider(),
                claims.providerSubject(),
                claims.email(),
                claims.displayName(),
                now,
                now);
    }

    /**
     * Applies newly verified profile claims without changing the external key.
     *
     * @param claims refreshed external profile claims
     * @param now profile update timestamp
     * @return updated Identity
     */
    public Identity updateProfile(IdentityClaims claims, Instant now) {
        if (claims == null) {
            throw new IdentityValidationException("Identity claims are required");
        }
        if (!provider.equals(claims.provider()) || !providerSubject.equals(claims.providerSubject())) {
            throw new IdentityValidationException("Identity provider key cannot change");
        }
        return new Identity(id, provider, providerSubject, claims.email(), claims.displayName(), createdAt, now);
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IdentityValidationException(field + " must not be blank");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IdentityValidationException(field + " must be at most " + maxLength + " characters");
        }
        return normalized;
    }

    private static String optionalText(String value, String field, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IdentityValidationException(field + " must be at most " + maxLength + " characters");
        }
        return normalized;
    }
}

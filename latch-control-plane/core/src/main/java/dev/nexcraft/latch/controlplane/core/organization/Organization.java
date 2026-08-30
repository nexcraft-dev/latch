package dev.nexcraft.latch.controlplane.core.organization;

import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationValidationException;
import java.time.Instant;
import java.util.UUID;

/**
 * Framework-free organization aggregate.
 *
 * @param id organization identifier
 * @param name display name
 * @param slug immutable URL-safe identifier
 * @param status lifecycle status
 * @param createdAt creation timestamp in UTC
 * @param updatedAt last update timestamp in UTC
 */
public record Organization(
        UUID id,
        String name,
        String slug,
        OrganizationStatus status,
        Instant createdAt,
        Instant updatedAt) {

    /** Maximum allowed organization name length. */
    public static final int MAX_NAME_LENGTH = 120;

    /** Maximum allowed generated slug length. */
    public static final int MAX_SLUG_LENGTH = 80;

    /**
     * Validates and normalizes aggregate values at the domain boundary.
     */
    public Organization {
        if (id == null) {
            throw new OrganizationValidationException("Organization id is required");
        }
        name = requireName(name);
        slug = requireSlug(slug);
        if (status == null) {
            throw new OrganizationValidationException("Organization status is required");
        }
        if (createdAt == null || updatedAt == null) {
            throw new OrganizationValidationException("Organization timestamps are required");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new OrganizationValidationException("Organization updatedAt cannot be before createdAt");
        }
    }

    /**
     * Creates a new active organization.
     *
     * @param id organization identifier
     * @param name display name
     * @param slug generated slug
     * @param now creation timestamp
     * @return a new active organization
     */
    public static Organization create(UUID id, String name, String slug, Instant now) {
        return new Organization(id, name, slug, OrganizationStatus.ACTIVE, now, now);
    }

    /**
     * Returns an organization with a changed name and the same immutable slug.
     *
     * @param newName replacement display name
     * @param now update timestamp
     * @return renamed organization
     */
    public Organization rename(String newName, Instant now) {
        return new Organization(id, newName, slug, status, createdAt, now);
    }

    /**
     * Returns a soft-deleted organization.
     *
     * @param now deletion timestamp
     * @return deleted organization
     */
    public Organization markDeleted(Instant now) {
        return new Organization(id, name, slug, OrganizationStatus.DELETED, createdAt, now);
    }

    /**
     * Indicates whether this organization is addressable by the active API.
     *
     * @return true when the organization is active
     */
    public boolean isActive() {
        return status == OrganizationStatus.ACTIVE;
    }

    private static String requireName(String value) {
        if (value == null || value.isBlank()) {
            throw new OrganizationValidationException("Organization name must not be blank");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new OrganizationValidationException("Organization name must be at most 120 characters");
        }
        return normalized;
    }

    private static String requireSlug(String value) {
        if (value == null || value.isBlank()) {
            throw new OrganizationValidationException("Organization slug is required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_SLUG_LENGTH) {
            throw new OrganizationValidationException("Organization slug must be at most 80 characters");
        }
        return normalized;
    }
}

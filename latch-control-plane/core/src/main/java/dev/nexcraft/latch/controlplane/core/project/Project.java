package dev.nexcraft.latch.controlplane.core.project;

import dev.nexcraft.latch.controlplane.core.project.error.ProjectValidationException;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Framework-free Project aggregate owned by exactly one Organization.
 *
 * @param id Project identifier
 * @param organizationId owning Organization identifier
 * @param name display name
 * @param key stable Organization-local configuration key
 * @param description optional Project description
 * @param status Project lifecycle status
 * @param createdAt creation timestamp in UTC
 * @param updatedAt last update timestamp in UTC
 */
public record Project(
        UUID id,
        UUID organizationId,
        String name,
        String key,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt) {

    /** Maximum allowed Project name length. */
    public static final int MAX_NAME_LENGTH = 120;

    /** Maximum allowed Project key length. */
    public static final int MAX_KEY_LENGTH = 80;

    /** Maximum allowed Project description length. */
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    /**
     * Validates and normalizes aggregate values at the domain boundary.
     */
    public Project {
        if (id == null) {
            throw new ProjectValidationException("Project id is required");
        }
        if (organizationId == null) {
            throw new ProjectValidationException("Project organization id is required");
        }
        name = requireName(name);
        key = requireKey(key);
        description = normalizeDescription(description);
        if (status == null) {
            throw new ProjectValidationException("Project status is required");
        }
        if (createdAt == null || updatedAt == null) {
            throw new ProjectValidationException("Project timestamps are required");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new ProjectValidationException("Project updatedAt cannot be before createdAt");
        }
    }

    /**
     * Creates a new active Project.
     *
     * @param id Project identifier
     * @param organizationId owning Organization identifier
     * @param name display name
     * @param key stable configuration key
     * @param description optional description
     * @param now creation timestamp
     * @return active Project
     */
    public static Project create(
            UUID id,
            UUID organizationId,
            String name,
            String key,
            String description,
            Instant now) {
        return new Project(id, organizationId, name, key, description, ProjectStatus.ACTIVE, now, now);
    }

    /**
     * Returns a Project with mutable display fields replaced.
     *
     * @param newName replacement display name
     * @param newDescription replacement description, or null to clear it
     * @param now update timestamp
     * @return updated Project
     */
    public Project update(String newName, String newDescription, Instant now) {
        return new Project(id, organizationId, newName, key, newDescription, status, createdAt, now);
    }

    /**
     * Returns a soft-deleted Project.
     *
     * @param now deletion timestamp
     * @return deleted Project
     */
    public Project markDeleted(Instant now) {
        return new Project(id, organizationId, name, key, description, ProjectStatus.DELETED, createdAt, now);
    }

    /**
     * Indicates whether this Project is addressable by active APIs.
     *
     * @return true when the Project is active
     */
    public boolean isActive() {
        return status == ProjectStatus.ACTIVE;
    }

    private static String requireName(String value) {
        if (value == null || value.isBlank()) {
            throw new ProjectValidationException("Project name must not be blank");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new ProjectValidationException("Project name must be at most 120 characters");
        }
        return normalized;
    }

    private static String requireKey(String value) {
        if (value == null || value.isBlank()) {
            throw new ProjectValidationException("Project key must not be blank");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_KEY_LENGTH) {
            throw new ProjectValidationException("Project key must be at most 80 characters");
        }
        if (!KEY_PATTERN.matcher(normalized).matches()) {
            throw new ProjectValidationException(
                    "Project key must contain only lowercase letters, numbers, and hyphens");
        }
        return normalized;
    }

    private static String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ProjectValidationException("Project description must be at most 500 characters");
        }
        return normalized;
    }
}

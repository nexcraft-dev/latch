package dev.nexcraft.latch.controlplane.core.organization.error;

/**
 * Indicates that a generated organization slug could not be made unique.
 */
public class OrganizationSlugConflictException extends RuntimeException {

    /**
     * Creates a slug conflict exception.
     *
     * @param slug conflicting slug
     */
    public OrganizationSlugConflictException(String slug) {
        super("Organization slug is already in use: " + slug);
    }
}

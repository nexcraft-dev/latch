package dev.nexcraft.latch.controlplane.core.organization.error;

import java.util.UUID;

/**
 * Indicates that an active organization could not be found.
 */
public class OrganizationNotFoundException extends RuntimeException {

    /**
     * Creates a not-found exception for an organization identifier.
     *
     * @param id requested organization identifier
     */
    public OrganizationNotFoundException(UUID id) {
        super("Organization was not found: " + id);
    }
}

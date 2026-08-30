package dev.nexcraft.latch.controlplane.core.organization.error;

/**
 * Indicates that an organization command violates a domain invariant.
 */
public class OrganizationValidationException extends RuntimeException {

    /**
     * Creates a validation exception.
     *
     * @param message safe client-facing validation detail
     */
    public OrganizationValidationException(String message) {
        super(message);
    }
}

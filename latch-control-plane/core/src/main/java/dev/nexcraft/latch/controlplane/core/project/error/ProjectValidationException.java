package dev.nexcraft.latch.controlplane.core.project.error;

/**
 * Indicates that a Project command violates a domain invariant.
 */
public class ProjectValidationException extends RuntimeException {

    /**
     * Creates a Project validation exception.
     *
     * @param message safe client-facing validation detail
     */
    public ProjectValidationException(String message) {
        super(message);
    }
}

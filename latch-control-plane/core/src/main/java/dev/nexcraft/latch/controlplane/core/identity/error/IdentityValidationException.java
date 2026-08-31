package dev.nexcraft.latch.controlplane.core.identity.error;

/**
 * Indicates that Identity data violates a domain invariant.
 */
public class IdentityValidationException extends RuntimeException {

    /**
     * Creates an Identity validation exception.
     *
     * @param message safe validation detail
     */
    public IdentityValidationException(String message) {
        super(message);
    }
}

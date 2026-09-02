package dev.nexcraft.latch.controlplane.core.membership.error;

/**
 * Indicates that membership input violates an application invariant.
 */
public class MembershipValidationException extends RuntimeException {

    /**
     * Creates a membership validation exception.
     *
     * @param message safe validation detail
     */
    public MembershipValidationException(String message) {
        super(message);
    }
}

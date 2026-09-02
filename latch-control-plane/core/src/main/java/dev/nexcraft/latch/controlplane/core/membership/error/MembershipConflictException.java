package dev.nexcraft.latch.controlplane.core.membership.error;

/**
 * Indicates that a membership operation conflicts with existing membership.
 */
public class MembershipConflictException extends RuntimeException {

    /**
     * Creates a membership conflict exception.
     *
     * @param message safe conflict detail
     */
    public MembershipConflictException(String message) {
        super(message);
    }
}

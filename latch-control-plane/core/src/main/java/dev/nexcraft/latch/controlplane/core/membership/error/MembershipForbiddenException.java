package dev.nexcraft.latch.controlplane.core.membership.error;

/**
 * Indicates that a member lacks the role required for an operation.
 */
public class MembershipForbiddenException extends RuntimeException {

    /**
     * Creates a membership authorization exception.
     *
     * @param message safe authorization detail
     */
    public MembershipForbiddenException(String message) {
        super(message);
    }
}

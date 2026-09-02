package dev.nexcraft.latch.controlplane.core.membership.error;

import java.util.UUID;

/**
 * Indicates that an Organization membership does not exist.
 */
public class MembershipNotFoundException extends RuntimeException {

    /**
     * Creates a membership-not-found exception.
     *
     * @param membershipId requested membership identifier
     */
    public MembershipNotFoundException(UUID membershipId) {
        super("Membership was not found: " + membershipId);
    }
}

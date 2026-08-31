package dev.nexcraft.latch.controlplane.core.identity.error;

import java.util.UUID;

/**
 * Indicates that an Identity does not exist.
 */
public class IdentityNotFoundException extends RuntimeException {

    /**
     * Creates an Identity-not-found exception.
     *
     * @param id requested Identity identifier
     */
    public IdentityNotFoundException(UUID id) {
        super("Identity was not found: " + id);
    }
}

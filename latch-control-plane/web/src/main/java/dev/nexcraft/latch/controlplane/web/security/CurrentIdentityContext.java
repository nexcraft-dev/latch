package dev.nexcraft.latch.controlplane.web.security;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.NotAuthorizedException;

/**
 * Holds the resolved core Identity for the current HTTP request.
 */
@RequestScoped
public class CurrentIdentityContext {

    private Identity identity;

    /**
     * Stores the Identity resolved from the verified request principal.
     *
     * @param resolvedIdentity resolved core Identity
     */
    public void set(Identity resolvedIdentity) {
        this.identity = resolvedIdentity;
    }

    /**
     * Returns the authenticated Identity or raises the standard bearer challenge.
     *
     * @return current core Identity
     */
    public Identity require() {
        if (identity == null) {
            throw new NotAuthorizedException("Bearer");
        }
        return identity;
    }
}

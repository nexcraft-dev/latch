package dev.nexcraft.latch.controlplane.core.identity.service;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.core.identity.IdentityClaims;

/**
 * Application contract for resolving authenticated external identities.
 */
public interface IdentityService {

    /**
     * Resolves an Identity by provider and subject, creating it when absent.
     *
     * @param claims verified external identity claims
     * @return resolved and current Identity
     */
    Identity resolveOrCreate(IdentityClaims claims);
}

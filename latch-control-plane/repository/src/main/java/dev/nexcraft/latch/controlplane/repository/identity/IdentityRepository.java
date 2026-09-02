package dev.nexcraft.latch.controlplane.repository.identity;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence boundary for externally authenticated Identities.
 */
public interface IdentityRepository {

    /**
     * Finds an Identity by its external provider key.
     *
     * @param provider verified provider issuer
     * @param providerSubject verified provider subject
     * @return matching Identity when present
     */
    Optional<Identity> findByProviderAndSubject(String provider, String providerSubject);

    /**
     * Finds an Identity by its internal identifier.
     *
     * @param id Identity identifier
     * @return matching Identity when present
     */
    Optional<Identity> findById(UUID id);

    /**
     * Persists an Identity.
     *
     * @param identity Identity to persist
     * @return persisted Identity
     */
    Identity save(Identity identity);
}

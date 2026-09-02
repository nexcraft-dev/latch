package dev.nexcraft.latch.controlplane.service.identity;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.core.identity.IdentityClaims;
import dev.nexcraft.latch.controlplane.core.identity.service.IdentityService;
import dev.nexcraft.latch.controlplane.repository.identity.IdentityRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Resolves and refreshes framework-free Identities from verified OIDC claims.
 */
public final class IdentityServiceImpl implements IdentityService {

    private final IdentityRepository repository;
    private final Clock clock;

    /**
     * Creates an Identity service implementation.
     *
     * @param repository Identity persistence boundary
     * @param clock clock used for profile timestamps
     */
    public IdentityServiceImpl(IdentityRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Resolves an Identity by issuer and subject, creating or refreshing profile data.
     *
     * @param claims verified external claims
     * @return resolved Identity
     */
    @Override
    public Identity resolveOrCreate(IdentityClaims claims) {
        Instant now = Instant.now(clock);
        Identity candidate = Identity.create(UUID.randomUUID(), claims, now);
        IdentityClaims normalizedClaims = new IdentityClaims(
                candidate.provider(),
                candidate.providerSubject(),
                candidate.email(),
                candidate.displayName());
        return repository.findByProviderAndSubject(candidate.provider(), candidate.providerSubject())
                .map(existing -> repository.save(existing.updateProfile(normalizedClaims, now)))
                .orElseGet(() -> repository.save(candidate));
    }
}

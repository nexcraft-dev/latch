package dev.nexcraft.latch.controlplane.service.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.core.identity.IdentityClaims;
import dev.nexcraft.latch.controlplane.repository.identity.IdentityRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies Identity resolution without starting Quarkus.
 */
class IdentityServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");

    private final FakeIdentityRepository repository = new FakeIdentityRepository();
    private final IdentityServiceImpl service = new IdentityServiceImpl(
            repository,
            Clock.fixed(NOW, ZoneOffset.UTC));

    /**
     * Confirms first resolution creates an Identity and subsequent resolution refreshes profile fields.
     */
    @Test
    void createsThenRefreshesIdentityByProviderSubject() {
        Identity created = service.resolveOrCreate(
                new IdentityClaims("https://issuer.example", "subject-1", "old@example.com", "Old Name"));
        Identity refreshed = service.resolveOrCreate(
                new IdentityClaims("https://issuer.example", "subject-1", "new@example.com", "New Name"));

        assertEquals(created.id(), refreshed.id());
        assertEquals("new@example.com", refreshed.email());
        assertEquals("New Name", refreshed.displayName());
        Identity otherProvider = service.resolveOrCreate(
                new IdentityClaims("https://other-issuer.example", "subject-1", null, null));
        assertNotEquals(created.id(), service.resolveOrCreate(
                new IdentityClaims("https://issuer.example", "subject-2", null, null)).id());
        assertNotEquals(created.id(), otherProvider.id());
    }

    private static final class FakeIdentityRepository implements IdentityRepository {

        private final Map<UUID, Identity> identities = new HashMap<>();

        @Override
        public Optional<Identity> findByProviderAndSubject(String provider, String providerSubject) {
            return identities.values().stream()
                    .filter(identity -> identity.provider().equals(provider))
                    .filter(identity -> identity.providerSubject().equals(providerSubject))
                    .findFirst();
        }

        @Override
        public Optional<Identity> findById(UUID id) {
            return Optional.ofNullable(identities.get(id));
        }

        @Override
        public Identity save(Identity identity) {
            identities.put(identity.id(), identity);
            return identity;
        }
    }
}

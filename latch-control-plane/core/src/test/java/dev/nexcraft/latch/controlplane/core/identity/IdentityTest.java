package dev.nexcraft.latch.controlplane.core.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.nexcraft.latch.controlplane.core.identity.error.IdentityValidationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies framework-free Identity invariants.
 */
class IdentityTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");

    /**
     * Confirms profile changes preserve the external Identity key.
     */
    @Test
    void updatesProfileWithoutChangingProviderKey() {
        Identity identity = Identity.create(
                UUID.randomUUID(),
                new IdentityClaims("https://issuer.example", "subject-1", "old@example.com", "Old Name"),
                NOW);

        Identity updated = identity.updateProfile(
                new IdentityClaims("https://issuer.example", "subject-1", "new@example.com", "New Name"),
                NOW.plusSeconds(1));

        assertEquals(identity.provider(), updated.provider());
        assertEquals(identity.providerSubject(), updated.providerSubject());
        assertEquals("new@example.com", updated.email());
        assertEquals("New Name", updated.displayName());
    }

    /**
     * Confirms the external provider subject is required.
     */
    @Test
    void rejectsBlankProviderSubject() {
        assertThrows(
                IdentityValidationException.class,
                () -> Identity.create(
                        UUID.randomUUID(),
                        new IdentityClaims("https://issuer.example", " ", null, null),
                        NOW));
    }
}

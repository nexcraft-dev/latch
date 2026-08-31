package dev.nexcraft.latch.controlplane.core.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.nexcraft.latch.controlplane.core.membership.error.MembershipValidationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies framework-free Membership invariants.
 */
class MembershipTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");

    /**
     * Confirms a membership can change roles while preserving its identity.
     */
    @Test
    void changesRoleWithoutChangingMembershipReferences() {
        UUID membershipId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID identityId = UUID.randomUUID();
        Membership membership = Membership.create(
                membershipId,
                organizationId,
                identityId,
                MembershipRole.OWNER,
                NOW);

        Membership updated = membership.changeRole(MembershipRole.ADMIN, NOW.plusSeconds(1));

        assertEquals(membershipId, updated.id());
        assertEquals(organizationId, updated.organizationId());
        assertEquals(identityId, updated.identityId());
        assertEquals(MembershipRole.ADMIN, updated.role());
    }

    /**
     * Confirms a membership role is required.
     */
    @Test
    void rejectsMissingRole() {
        assertThrows(
                MembershipValidationException.class,
                () -> Membership.create(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        NOW));
    }
}

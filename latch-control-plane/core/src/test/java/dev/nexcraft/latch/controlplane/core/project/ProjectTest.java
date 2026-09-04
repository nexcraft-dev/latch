package dev.nexcraft.latch.controlplane.core.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.nexcraft.latch.controlplane.core.project.error.ProjectValidationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies framework-free Project invariants.
 */
class ProjectTest {

    private static final Instant NOW = Instant.parse("2026-09-02T10:00:00Z");

    /**
     * Confirms mutable updates preserve Project identity and key.
     */
    @Test
    void updatesMutableFieldsWithoutChangingStableReferences() {
        UUID projectId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        Project project = Project.create(projectId, organizationId, "Checkout", "checkout", "Initial", NOW);

        Project updated = project.update("Checkout Platform", "Updated", NOW.plusSeconds(1));

        assertEquals(projectId, updated.id());
        assertEquals(organizationId, updated.organizationId());
        assertEquals("checkout", updated.key());
        assertEquals("Checkout Platform", updated.name());
        assertEquals("Updated", updated.description());
    }

    /**
     * Confirms a blank description is normalized to no description.
     */
    @Test
    void normalizesBlankDescription() {
        Project project = Project.create(
                UUID.randomUUID(), UUID.randomUUID(), "Checkout", "checkout", " ", NOW);

        assertNull(project.description());
    }

    /**
     * Confirms invalid Project keys are rejected.
     */
    @Test
    void rejectsInvalidKey() {
        assertThrows(
                ProjectValidationException.class,
                () -> Project.create(
                        UUID.randomUUID(), UUID.randomUUID(), "Checkout", "Checkout App", null, NOW));
    }

    /**
     * Confirms soft deletion changes only lifecycle state and update time.
     */
    @Test
    void marksProjectDeletedWithoutChangingIdentity() {
        Project project = Project.create(
                UUID.randomUUID(), UUID.randomUUID(), "Checkout", "checkout", null, NOW);

        Project deleted = project.markDeleted(NOW.plusSeconds(1));

        assertEquals(ProjectStatus.DELETED, deleted.status());
        assertEquals(project.id(), deleted.id());
        assertEquals(project.organizationId(), deleted.organizationId());
        assertEquals(project.key(), deleted.key());
    }
}

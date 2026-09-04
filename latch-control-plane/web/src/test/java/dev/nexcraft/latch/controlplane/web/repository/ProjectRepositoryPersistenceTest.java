package dev.nexcraft.latch.controlplane.web.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectSortDirection;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectSortField;
import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import dev.nexcraft.latch.controlplane.repository.project.ProjectRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies Project persistence against the isolated H2 profile.
 */
@QuarkusTest
class ProjectRepositoryPersistenceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T10:00:00Z");

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    ProjectRepository projectRepository;

    /**
     * Confirms active listing searches, sorts, paginates, and excludes deleted rows.
     */
    @Test
    void listsActiveProjectsWithSearchSortAndPagination() {
        Organization organization = saveOrganization();
        String marker = "repo-" + UUID.randomUUID();
        Project zebra = project(
                organization.id(), marker + " Zebra", marker + "-zebra", "Zebra description", NOW.plusSeconds(1));
        Project alpha = project(
                organization.id(), marker + " Alpha", marker + "-alpha", "Alpha description", NOW.plusSeconds(2));
        Project deleted = project(
                organization.id(), marker + " Deleted", marker + "-deleted", "Deleted description", NOW.plusSeconds(3));
        projectRepository.save(zebra);
        projectRepository.save(alpha);
        projectRepository.save(deleted.markDeleted(NOW.plusSeconds(4)));

        ProjectQuery query = new ProjectQuery(
                marker.toUpperCase(), 0, 1, ProjectSortField.NAME, ProjectSortDirection.ASC);

        var page = projectRepository.findActiveByOrganization(organization.id(), query);

        assertEquals(2, page.totalElements());
        assertEquals(2, page.totalPages());
        assertEquals(1, page.items().size());
        assertTrue(page.items().get(0).name().endsWith(" Alpha"));
        assertTrue(projectRepository
                .findActiveByOrganizationAndId(organization.id(), deleted.id())
                .isEmpty());
    }

    /**
     * Confirms a Project key is reusable across Organizations but not within one.
     */
    @Test
    void scopesProjectKeysAndReadsByOrganization() {
        Organization first = saveOrganization();
        Organization second = saveOrganization();
        String key = "shared-" + UUID.randomUUID();
        Project firstProject = project(first.id(), "First Project", key, null, NOW);
        Project secondProject = project(second.id(), "Second Project", key, null, NOW);

        projectRepository.save(firstProject);
        projectRepository.save(secondProject);

        assertTrue(projectRepository.existsByOrganizationAndKey(first.id(), key));
        assertTrue(projectRepository.existsByOrganizationAndKey(second.id(), key));
        assertTrue(projectRepository
                .findActiveByOrganizationAndId(second.id(), firstProject.id())
                .isEmpty());
    }

    private Organization saveOrganization() {
        String suffix = UUID.randomUUID().toString();
        return organizationRepository.save(Organization.create(
                UUID.randomUUID(), "Repository Organization " + suffix, "repository-" + suffix, NOW));
    }

    private Project project(
            UUID organizationId,
            String name,
            String key,
            String description,
            Instant timestamp) {
        return Project.create(UUID.randomUUID(), organizationId, name, key, description, timestamp);
    }
}

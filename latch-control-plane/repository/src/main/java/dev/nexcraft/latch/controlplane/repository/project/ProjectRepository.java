package dev.nexcraft.latch.controlplane.repository.project;

import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectPage;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectQuery;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence boundary for Organization-owned Projects.
 */
public interface ProjectRepository {

    /**
     * Checks whether a Project key is already used in an Organization.
     *
     * @param organizationId owning Organization
     * @param key candidate Project key
     * @return true when the key is already used
     */
    boolean existsByOrganizationAndKey(UUID organizationId, String key);

    /**
     * Finds an active Project through its Organization path.
     *
     * @param organizationId owning Organization
     * @param projectId Project identifier
     * @return matching active Project when present
     */
    Optional<Project> findActiveByOrganizationAndId(UUID organizationId, UUID projectId);

    /**
     * Lists active Projects in one Organization.
     *
     * @param organizationId owning Organization
     * @param query validated Project query
     * @return a page of active Projects
     */
    ProjectPage findActiveByOrganization(UUID organizationId, ProjectQuery query);

    /**
     * Persists a Project aggregate.
     *
     * @param project Project aggregate to persist
     * @return persisted Project
     */
    Project save(Project project);
}

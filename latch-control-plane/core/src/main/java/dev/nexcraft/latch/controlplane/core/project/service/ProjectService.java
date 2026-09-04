package dev.nexcraft.latch.controlplane.core.project.service;

import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.command.CreateProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.command.DeleteProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.command.UpdateProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.query.GetProjectQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ListProjectsQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectPage;

/**
 * Framework-independent application contract for Project use cases.
 */
public interface ProjectService {

    /**
     * Creates a Project in an active Organization.
     *
     * @param command Project creation input
     * @return created Project
     */
    Project create(CreateProjectCommand command);

    /**
     * Lists active Projects visible to the authenticated caller.
     *
     * @param query Project listing input
     * @return active Project page
     */
    ProjectPage list(ListProjectsQuery query);

    /**
     * Gets an active Project through its Organization path.
     *
     * @param query Project lookup input
     * @return active Project
     */
    Project get(GetProjectQuery query);

    /**
     * Updates mutable Project fields without changing its key or owner.
     *
     * @param command Project update input
     * @return updated Project
     */
    Project update(UpdateProjectCommand command);

    /**
     * Soft-deletes a Project.
     *
     * @param command Project deletion input
     */
    void delete(DeleteProjectCommand command);
}

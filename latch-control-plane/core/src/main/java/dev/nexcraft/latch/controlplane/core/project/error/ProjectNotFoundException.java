package dev.nexcraft.latch.controlplane.core.project.error;

import java.util.UUID;

/**
 * Indicates that an active Project could not be found in the requested Organization.
 */
public class ProjectNotFoundException extends RuntimeException {

    /**
     * Creates a not-found exception for a Project identifier.
     *
     * @param projectId requested Project identifier
     */
    public ProjectNotFoundException(UUID projectId) {
        super("Project was not found: " + projectId);
    }
}

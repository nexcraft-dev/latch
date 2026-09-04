package dev.nexcraft.latch.controlplane.core.project.error;

/**
 * Indicates that a Project key is already used in an Organization.
 */
public class ProjectKeyConflictException extends RuntimeException {

    /**
     * Creates a Project key conflict exception.
     *
     * @param key conflicting Project key
     */
    public ProjectKeyConflictException(String key) {
        super("Project key is already in use: " + key);
    }
}

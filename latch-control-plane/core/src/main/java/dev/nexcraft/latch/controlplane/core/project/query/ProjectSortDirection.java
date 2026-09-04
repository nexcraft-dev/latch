package dev.nexcraft.latch.controlplane.core.project.query;

import dev.nexcraft.latch.controlplane.core.project.error.ProjectValidationException;

/**
 * Allow-listed Project sort directions.
 */
public enum ProjectSortDirection {
    ASC("asc"),
    DESC("desc");

    private final String value;

    ProjectSortDirection(String value) {
        this.value = value;
    }

    /**
     * Returns the public query parameter value.
     *
     * @return API direction value
     */
    public String value() {
        return value;
    }

    /**
     * Parses a public Project sort direction.
     *
     * @param value requested direction
     * @return allow-listed direction
     */
    public static ProjectSortDirection parse(String value) {
        for (ProjectSortDirection direction : values()) {
            if (direction.value.equals(value)) {
                return direction;
            }
        }
        throw new ProjectValidationException("Unsupported Project sort direction: " + value);
    }
}

package dev.nexcraft.latch.controlplane.core.project.query;

import dev.nexcraft.latch.controlplane.core.project.error.ProjectValidationException;

/**
 * Allow-listed Project sort fields.
 */
public enum ProjectSortField {
    NAME("name"),
    KEY("key"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String value;

    ProjectSortField(String value) {
        this.value = value;
    }

    /**
     * Returns the public query parameter value.
     *
     * @return API field value
     */
    public String value() {
        return value;
    }

    /**
     * Parses a public Project sort field.
     *
     * @param value requested field
     * @return allow-listed field
     */
    public static ProjectSortField parse(String value) {
        for (ProjectSortField field : values()) {
            if (field.value.equals(value)) {
                return field;
            }
        }
        throw new ProjectValidationException("Unsupported Project sort field: " + value);
    }
}

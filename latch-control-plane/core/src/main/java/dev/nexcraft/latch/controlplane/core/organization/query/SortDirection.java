package dev.nexcraft.latch.controlplane.core.organization.query;

import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationValidationException;

/**
 * Allow-listed sort directions.
 */
public enum SortDirection {
    ASC("asc"),
    DESC("desc");

    private final String value;

    SortDirection(String value) {
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
     * Parses a public sort direction.
     *
     * @param value requested direction
     * @return allow-listed direction
     */
    public static SortDirection parse(String value) {
        for (SortDirection direction : values()) {
            if (direction.value.equals(value)) {
                return direction;
            }
        }
        throw new OrganizationValidationException("Unsupported organization sort direction: " + value);
    }
}

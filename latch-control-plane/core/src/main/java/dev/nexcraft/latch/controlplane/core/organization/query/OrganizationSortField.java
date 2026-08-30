package dev.nexcraft.latch.controlplane.core.organization.query;

import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationValidationException;

/**
 * Allow-listed organization sort fields.
 */
public enum OrganizationSortField {
    NAME("name"),
    SLUG("slug"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String value;

    OrganizationSortField(String value) {
        this.value = value;
    }

    /**
     * Returns the public query parameter value.
     *
     * @return API sort field value
     */
    public String value() {
        return value;
    }

    /**
     * Parses a public sort field.
     *
     * @param value requested field
     * @return allow-listed field
     */
    public static OrganizationSortField parse(String value) {
        for (OrganizationSortField field : values()) {
            if (field.value.equals(value)) {
                return field;
            }
        }
        throw new OrganizationValidationException("Unsupported organization sort field: " + value);
    }
}

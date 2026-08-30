package dev.nexcraft.latch.controlplane.core.organization.query;

import java.util.Objects;

/**
 * Validated repository query for active organizations.
 *
 * @param search normalized optional search text
 * @param page zero-based page number
 * @param size requested page size
 * @param sortField allow-listed sort field
 * @param direction allow-listed sort direction
 */
public record OrganizationQuery(
        String search,
        int page,
        int size,
        OrganizationSortField sortField,
        SortDirection direction) {

    /**
     * Validates repository query values.
     */
    public OrganizationQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        Objects.requireNonNull(sortField, "sortField is required");
        Objects.requireNonNull(direction, "direction is required");
    }
}

package dev.nexcraft.latch.controlplane.core.membership.query;

import java.util.UUID;

/**
 * Validated repository query for Organization memberships.
 *
 * @param organizationId target Organization identifier
 * @param page zero-based page number
 * @param size requested page size
 */
public record ListMembershipsQuery(UUID organizationId, int page, int size) {

    /**
     * Validates pagination values.
     */
    public ListMembershipsQuery {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId is required");
        }
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
    }
}

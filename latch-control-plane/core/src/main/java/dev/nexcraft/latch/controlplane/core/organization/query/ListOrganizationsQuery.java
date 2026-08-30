package dev.nexcraft.latch.controlplane.core.organization.query;

/**
 * Input model for listing active organizations.
 *
 * @param search optional name or slug search text
 * @param page zero-based page number
 * @param size requested page size
 * @param sort field and direction expression
 */
public record ListOrganizationsQuery(String search, Integer page, Integer size, String sort) {
}

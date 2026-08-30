package dev.nexcraft.latch.controlplane.core.organization.dto;

import java.util.List;

/**
 * Paginated organization data model.
 *
 * @param items organizations in the requested page
 * @param page zero-based page number
 * @param size requested page size
 * @param totalElements total number of matching organizations
 * @param totalPages total number of available pages
 */
public record OrganizationPage(
        List<OrganizationDetails> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /**
     * Creates an immutable page model.
     */
    public OrganizationPage {
        items = List.copyOf(items);
    }
}

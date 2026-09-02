package dev.nexcraft.latch.controlplane.core.membership.dto;

import java.util.List;

/**
 * Paginated Organization member data.
 *
 * @param items members in the requested page
 * @param page zero-based page number
 * @param size requested page size
 * @param totalElements total number of active members
 * @param totalPages total number of available pages
 */
public record MemberPage(
        List<OrganizationMember> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /**
     * Creates an immutable member page.
     */
    public MemberPage {
        items = List.copyOf(items);
    }
}

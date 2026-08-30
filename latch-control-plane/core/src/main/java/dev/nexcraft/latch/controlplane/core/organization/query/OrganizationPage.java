package dev.nexcraft.latch.controlplane.core.organization.query;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import java.util.List;

/**
 * Page of organization aggregates and its pagination metadata.
 *
 * @param items page items
 * @param page zero-based page number
 * @param size requested page size
 * @param totalElements total number of matching elements
 */
public record OrganizationPage(List<Organization> items, int page, int size, long totalElements) {

    /**
     * Creates an immutable page.
     */
    public OrganizationPage {
        items = List.copyOf(items);
    }

    /**
     * Calculates the number of pages using the requested page size.
     *
     * @return total page count, or zero when there are no elements
     */
    public int totalPages() {
        return totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
    }
}

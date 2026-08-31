package dev.nexcraft.latch.controlplane.core.membership.query;

import dev.nexcraft.latch.controlplane.core.membership.Membership;
import java.util.List;

/**
 * Paginated membership aggregates returned by the repository boundary.
 *
 * @param items memberships in the requested page
 * @param page zero-based page number
 * @param size requested page size
 * @param totalElements total number of memberships
 */
public record MembershipPage(List<Membership> items, int page, int size, long totalElements) {

    /**
     * Creates an immutable membership page.
     */
    public MembershipPage {
        items = List.copyOf(items);
    }

    /**
     * Calculates the number of available pages.
     *
     * @return total page count, or zero when there are no memberships
     */
    public int totalPages() {
        return totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
    }
}

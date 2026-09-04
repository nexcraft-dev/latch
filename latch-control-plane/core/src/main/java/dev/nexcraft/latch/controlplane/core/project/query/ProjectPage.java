package dev.nexcraft.latch.controlplane.core.project.query;

import dev.nexcraft.latch.controlplane.core.project.Project;
import java.util.List;

/**
 * Page of Project aggregates and pagination metadata.
 *
 * @param items Projects in the requested page
 * @param page zero-based page number
 * @param size requested page size
 * @param totalElements total matching active Projects
 */
public record ProjectPage(List<Project> items, int page, int size, long totalElements) {

    /**
     * Creates an immutable page.
     */
    public ProjectPage {
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

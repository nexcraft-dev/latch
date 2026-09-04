package dev.nexcraft.latch.controlplane.core.project.dto;

import java.util.List;

/**
 * Paginated Project data model.
 *
 * @param items Projects in the requested page
 * @param page zero-based page number
 * @param size requested page size
 * @param totalElements total number of matching Projects
 * @param totalPages total number of available pages
 */
public record ProjectPage(
        List<ProjectDetails> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /**
     * Creates an immutable page model.
     */
    public ProjectPage {
        items = List.copyOf(items);
    }
}

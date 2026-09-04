package dev.nexcraft.latch.controlplane.repository.project.persistence;

import dev.nexcraft.latch.controlplane.core.project.Project;

/**
 * Explicit mapper between the framework-free Project aggregate and JPA entity.
 */
public final class ProjectEntityMapper {

    /**
     * Converts an entity to the domain aggregate.
     *
     * @param entity persistence entity
     * @return Project aggregate
     */
    public Project toDomain(ProjectEntity entity) {
        return new Project(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getName(),
                entity.getKey(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Converts a Project aggregate to a new persistence entity.
     *
     * @param project Project aggregate
     * @return persistence entity
     */
    public ProjectEntity toEntity(Project project) {
        ProjectEntity entity = new ProjectEntity(project.id());
        entity.apply(project);
        return entity;
    }
}

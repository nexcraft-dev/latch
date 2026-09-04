package dev.nexcraft.latch.controlplane.repository.project.persistence;

import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of an Organization-owned Project.
 */
@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "\"key\"", nullable = false, length = 80)
    private String key;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Creates an empty entity for JPA.
     */
    protected ProjectEntity() {
    }

    /**
     * Creates an entity with a Project identity.
     *
     * @param id Project identifier
     */
    public ProjectEntity(UUID id) {
        this.id = id;
    }

    /**
     * Returns the Project identifier.
     *
     * @return Project identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the owning Organization identifier.
     *
     * @return Organization identifier
     */
    public UUID getOrganizationId() {
        return organizationId;
    }

    /**
     * Returns the Project display name.
     *
     * @return display name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the stable Project key.
     *
     * @return Project key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the optional Project description.
     *
     * @return Project description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the Project lifecycle status.
     *
     * @return Project status
     */
    public ProjectStatus getStatus() {
        return status;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the last update timestamp.
     *
     * @return update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Applies a Project aggregate to this persistence row.
     *
     * @param project source aggregate
     */
    public void apply(Project project) {
        this.organizationId = project.organizationId();
        this.name = project.name();
        this.key = project.key();
        this.description = project.description();
        this.status = project.status();
        this.createdAt = project.createdAt();
        this.updatedAt = project.updatedAt();
    }
}

package dev.nexcraft.latch.controlplane.repository.organization.persistence;

import dev.nexcraft.latch.controlplane.core.organization.OrganizationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of an organization.
 */
@Entity
@Table(name = "organizations")
public class OrganizationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "slug", nullable = false, length = 80, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Creates an empty entity for JPA.
     */
    protected OrganizationEntity() {
    }

    /**
     * Creates an entity with an organization identity.
     *
     * @param id organization identifier
     */
    public OrganizationEntity(UUID id) {
        this.id = id;
    }

    /**
     * Returns the organization identifier.
     *
     * @return organization identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the display name.
     *
     * @return display name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the immutable slug.
     *
     * @return organization slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Returns the lifecycle status.
     *
     * @return organization status
     */
    public OrganizationStatus getStatus() {
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
     * Applies the aggregate state to the persistence row.
     *
     * @param organization source aggregate
     */
    public void apply(dev.nexcraft.latch.controlplane.core.organization.Organization organization) {
        this.name = organization.name();
        this.slug = organization.slug();
        this.status = organization.status();
        this.createdAt = organization.createdAt();
        this.updatedAt = organization.updatedAt();
    }
}

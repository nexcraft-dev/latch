package dev.nexcraft.latch.controlplane.repository.membership.persistence;

import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of an Organization membership.
 */
@Entity
@Table(name = "organization_memberships")
public class OrganizationMembershipEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "identity_id", nullable = false)
    private UUID identityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MembershipRole role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Creates an empty entity for JPA.
     */
    protected OrganizationMembershipEntity() {
    }

    /**
     * Creates an entity with a membership identifier.
     *
     * @param id membership identifier
     */
    public OrganizationMembershipEntity(UUID id) {
        this.id = id;
    }

    /**
     * Returns the membership identifier.
     *
     * @return membership identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the Organization identifier.
     *
     * @return Organization identifier
     */
    public UUID getOrganizationId() {
        return organizationId;
    }

    /**
     * Returns the Identity identifier.
     *
     * @return Identity identifier
     */
    public UUID getIdentityId() {
        return identityId;
    }

    /**
     * Returns the membership role.
     *
     * @return membership role
     */
    public MembershipRole getRole() {
        return role;
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
     * Applies a membership to the persistence row.
     *
     * @param membership source membership
     */
    public void apply(Membership membership) {
        this.organizationId = membership.organizationId();
        this.identityId = membership.identityId();
        this.role = membership.role();
        this.createdAt = membership.createdAt();
        this.updatedAt = membership.updatedAt();
    }
}

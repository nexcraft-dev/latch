package dev.nexcraft.latch.controlplane.repository.membership.persistence;

import dev.nexcraft.latch.controlplane.core.membership.Membership;

/**
 * Explicit mapper between membership domain data and its JPA entity.
 */
public final class OrganizationMembershipEntityMapper {

    /**
     * Converts an entity to the framework-free membership model.
     *
     * @param entity membership persistence entity
     * @return membership domain model
     */
    public Membership toDomain(OrganizationMembershipEntity entity) {
        return new Membership(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getIdentityId(),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Converts a membership domain model to a new entity.
     *
     * @param membership membership domain model
     * @return persistence entity
     */
    public OrganizationMembershipEntity toEntity(Membership membership) {
        OrganizationMembershipEntity entity = new OrganizationMembershipEntity(membership.id());
        entity.apply(membership);
        return entity;
    }
}

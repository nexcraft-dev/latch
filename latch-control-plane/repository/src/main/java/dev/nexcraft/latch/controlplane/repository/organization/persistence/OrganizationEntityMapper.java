package dev.nexcraft.latch.controlplane.repository.organization.persistence;

import dev.nexcraft.latch.controlplane.core.organization.Organization;

/**
 * Explicit mapper between the framework-free aggregate and JPA entity.
 */
public final class OrganizationEntityMapper {

    /**
     * Converts an entity to the domain aggregate.
     *
     * @param entity persistence entity
     * @return domain aggregate
     */
    public Organization toDomain(OrganizationEntity entity) {
        return new Organization(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Converts an aggregate to a new persistence entity.
     *
     * @param organization domain aggregate
     * @return persistence entity
     */
    public OrganizationEntity toEntity(Organization organization) {
        OrganizationEntity entity = new OrganizationEntity(organization.id());
        entity.apply(organization);
        return entity;
    }
}

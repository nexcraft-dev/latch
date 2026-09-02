package dev.nexcraft.latch.controlplane.repository.identity.persistence;

import dev.nexcraft.latch.controlplane.core.identity.Identity;

/**
 * Explicit mapper between Identity domain data and its JPA entity.
 */
public final class IdentityEntityMapper {

    /**
     * Converts an entity to the framework-free Identity model.
     *
     * @param entity Identity persistence entity
     * @return Identity domain model
     */
    public Identity toDomain(IdentityEntity entity) {
        return new Identity(
                entity.getId(),
                entity.getProvider(),
                entity.getProviderSubject(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Converts an Identity domain model to a new entity.
     *
     * @param identity Identity domain model
     * @return persistence entity
     */
    public IdentityEntity toEntity(Identity identity) {
        IdentityEntity entity = new IdentityEntity(identity.id());
        entity.apply(identity);
        return entity;
    }
}

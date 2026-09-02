package dev.nexcraft.latch.controlplane.repository.identity.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Panache access object for Identity rows.
 */
@ApplicationScoped
public class IdentityPanacheRepository implements PanacheRepositoryBase<IdentityEntity, UUID> {
}

package dev.nexcraft.latch.controlplane.repository.organization.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Panache access object kept inside the persistence adapter.
 */
@ApplicationScoped
public class OrganizationPanacheRepository implements PanacheRepositoryBase<OrganizationEntity, UUID> {
}

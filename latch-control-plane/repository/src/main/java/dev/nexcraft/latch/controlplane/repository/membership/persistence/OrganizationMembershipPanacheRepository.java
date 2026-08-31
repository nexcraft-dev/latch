package dev.nexcraft.latch.controlplane.repository.membership.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Panache access object for Organization membership rows.
 */
@ApplicationScoped
public class OrganizationMembershipPanacheRepository
        implements PanacheRepositoryBase<OrganizationMembershipEntity, UUID> {
}

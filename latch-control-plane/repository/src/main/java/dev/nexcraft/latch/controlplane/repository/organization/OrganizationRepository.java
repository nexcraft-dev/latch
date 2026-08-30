package dev.nexcraft.latch.controlplane.repository.organization;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence boundary for active organization use cases.
 */
public interface OrganizationRepository {

    /**
     * Checks whether any organization, including deleted organizations, owns a slug.
     *
     * @param slug candidate slug
     * @return true when the slug is already used
     */
    boolean existsBySlug(String slug);

    /**
     * Finds an active organization by identifier.
     *
     * @param id organization identifier
     * @return the active organization when present
     */
    Optional<Organization> findActiveById(UUID id);

    /**
     * Lists active organizations according to a typed query.
     *
     * @param query validated list query
     * @return a page of active organizations
     */
    OrganizationPage findActive(OrganizationQuery query);

    /**
     * Persists an organization aggregate.
     *
     * @param organization aggregate to persist
     * @return persisted aggregate
     */
    Organization save(Organization organization);
}

package dev.nexcraft.latch.controlplane.repository.organization;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.membership.Membership;
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
     * Lists active Organizations associated with an Identity.
     *
     * @param identityId authenticated Identity
     * @param query validated list query
     * @return a page of accessible active Organizations
     */
    OrganizationPage findActiveForIdentity(UUID identityId, OrganizationQuery query);

    /**
     * Finds an active Organization associated with an Identity.
     *
     * @param organizationId target Organization
     * @param identityId authenticated Identity
     * @return the accessible active Organization when present
     */
    Optional<Organization> findActiveByIdForIdentity(UUID organizationId, UUID identityId);

    /**
     * Persists an organization aggregate.
     *
     * @param organization aggregate to persist
     * @return persisted aggregate
     */
    Organization save(Organization organization);

    /**
     * Persists a new Organization and its OWNER Membership atomically.
     *
     * @param organization Organization to persist
     * @param ownerMembership required OWNER membership
     * @return persisted Organization
     */
    Organization saveWithOwner(Organization organization, Membership ownerMembership);
}

package dev.nexcraft.latch.controlplane.repository.membership;

import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.query.ListMembershipsQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.MembershipPage;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence boundary for Organization memberships.
 */
public interface OrganizationMembershipRepository {

    /**
     * Finds a membership by Organization and membership identifiers.
     *
     * @param organizationId target Organization
     * @param membershipId membership identifier
     * @return matching membership when present
     */
    Optional<Membership> findByOrganizationAndId(UUID organizationId, UUID membershipId);

    /**
     * Finds a membership for an Identity in an Organization.
     *
     * @param organizationId target Organization
     * @param identityId member Identity
     * @return matching membership when present
     */
    Optional<Membership> findByOrganizationAndIdentity(UUID organizationId, UUID identityId);

    /**
     * Lists active membership rows for an Organization.
     *
     * @param query validated membership query
     * @return membership page
     */
    MembershipPage findByOrganization(ListMembershipsQuery query);

    /**
     * Persists a membership.
     *
     * @param membership membership to persist
     * @return persisted membership
     */
    Membership save(Membership membership);

    /**
     * Deletes a membership row without deleting its Identity.
     *
     * @param membership membership to delete
     */
    void delete(Membership membership);
}

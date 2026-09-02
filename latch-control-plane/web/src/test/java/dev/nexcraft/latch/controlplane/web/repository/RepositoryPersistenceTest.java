package dev.nexcraft.latch.controlplane.web.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.core.identity.IdentityClaims;
import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.query.ListMembershipsQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.MembershipPage;
import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationSortField;
import dev.nexcraft.latch.controlplane.core.organization.query.SortDirection;
import dev.nexcraft.latch.controlplane.repository.identity.IdentityRepository;
import dev.nexcraft.latch.controlplane.repository.membership.OrganizationMembershipRepository;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies Identity, membership, and Organization persistence against isolated H2.
 */
@QuarkusTest
class RepositoryPersistenceTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");

    @Inject
    IdentityRepository identityRepository;

    @Inject
    OrganizationMembershipRepository membershipRepository;

    @Inject
    OrganizationRepository organizationRepository;

    /**
     * Confirms external identity lookup uses provider and subject together.
     */
    @Test
    void findsIdentityByProviderAndSubject() {
        Identity identity = identity("provider-a", "subject-" + UUID.randomUUID());
        identityRepository.save(identity);

        assertEquals(
                identity.id(),
                identityRepository.findByProviderAndSubject(identity.provider(), identity.providerSubject())
                        .orElseThrow()
                        .id());
        assertTrue(identityRepository.findByProviderAndSubject("provider-b", identity.providerSubject()).isEmpty());
    }

    /**
     * Confirms an Organization can be persisted with its OWNER membership atomically.
     */
    @Test
    void savesOrganizationAndOwnerMembershipTogether() {
        Identity owner = identity("provider-a", "owner-" + UUID.randomUUID());
        identityRepository.save(owner);
        Organization organization = organization("Atomic Organization");
        Membership ownerMembership = Membership.create(
                UUID.randomUUID(), organization.id(), owner.id(), MembershipRole.OWNER, NOW);

        organizationRepository.saveWithOwner(organization, ownerMembership);

        assertTrue(organizationRepository.findActiveByIdForIdentity(organization.id(), owner.id()).isPresent());
        assertEquals(
                ownerMembership.id(),
                membershipRepository.findByOrganizationAndIdentity(organization.id(), owner.id())
                        .orElseThrow()
                        .id());
    }

    /**
     * Confirms active Organization reads are scoped through membership rows.
     */
    @Test
    void scopesOrganizationsToIdentityMemberships() {
        Identity owner = identity("provider-a", "scoped-owner-" + UUID.randomUUID());
        Identity outsider = identity("provider-a", "scoped-outsider-" + UUID.randomUUID());
        identityRepository.save(owner);
        identityRepository.save(outsider);
        Organization organization = organization("Scoped Organization");
        organizationRepository.saveWithOwner(
                organization,
                Membership.create(UUID.randomUUID(), organization.id(), owner.id(), MembershipRole.OWNER, NOW));

        OrganizationQuery query = new OrganizationQuery(null, 0, 20, OrganizationSortField.NAME, SortDirection.ASC);

        assertEquals(1, organizationRepository.findActiveForIdentity(owner.id(), query).totalElements());
        assertEquals(0, organizationRepository.findActiveForIdentity(outsider.id(), query).totalElements());
    }

    /**
     * Confirms membership listing and deletion leave the Identity intact.
     */
    @Test
    void listsAndDeletesMembershipWithoutDeletingIdentity() {
        Identity member = identity("provider-a", "member-" + UUID.randomUUID());
        identityRepository.save(member);
        Organization organization = organization("Membership Organization");
        organizationRepository.save(organization);
        Membership membership = Membership.create(
                UUID.randomUUID(), organization.id(), member.id(), MembershipRole.MEMBER, NOW);
        membershipRepository.save(membership);

        MembershipPage page = membershipRepository.findByOrganization(new ListMembershipsQuery(organization.id(), 0, 20));
        assertEquals(1, page.totalElements());

        membershipRepository.delete(membership);

        assertTrue(membershipRepository.findByOrganizationAndId(organization.id(), membership.id()).isEmpty());
        assertTrue(identityRepository.findById(member.id()).isPresent());
    }

    /**
     * Confirms soft-deleted Organizations are excluded from identity-scoped reads.
     */
    @Test
    void excludesDeletedOrganizationsFromScopedReads() {
        Identity owner = identity("provider-a", "deleted-owner-" + UUID.randomUUID());
        identityRepository.save(owner);
        Organization organization = organization("Deleted Organization");
        organizationRepository.saveWithOwner(
                organization,
                Membership.create(UUID.randomUUID(), organization.id(), owner.id(), MembershipRole.OWNER, NOW));
        organizationRepository.save(organization.markDeleted(NOW.plusSeconds(1)));

        assertEquals(
                0,
                organizationRepository.findActiveForIdentity(
                                owner.id(),
                                new OrganizationQuery(null, 0, 20, OrganizationSortField.NAME, SortDirection.ASC))
                        .totalElements());
        assertTrue(organizationRepository.findActiveByIdForIdentity(organization.id(), owner.id()).isEmpty());
    }

    private Identity identity(String provider, String subject) {
        return Identity.create(
                UUID.randomUUID(),
                new IdentityClaims(provider, subject, "user@example.com", "User"),
                NOW);
    }

    private Organization organization(String name) {
        return Organization.create(UUID.randomUUID(), name, name.toLowerCase().replace(' ', '-'), NOW);
    }
}

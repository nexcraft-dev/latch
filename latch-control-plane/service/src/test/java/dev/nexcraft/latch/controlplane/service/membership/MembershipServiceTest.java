package dev.nexcraft.latch.controlplane.service.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.core.identity.IdentityClaims;
import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.command.AddOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.RemoveOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.UpdateOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.dto.MemberPage;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipConflictException;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipForbiddenException;
import dev.nexcraft.latch.controlplane.core.membership.query.ListMembershipsQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.ListOrganizationMembersQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.MembershipPage;
import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import dev.nexcraft.latch.controlplane.repository.identity.IdentityRepository;
import dev.nexcraft.latch.controlplane.repository.membership.OrganizationMembershipRepository;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies initial membership authorization rules without starting Quarkus.
 */
class MembershipServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");

    private final FakeIdentityRepository identityRepository = new FakeIdentityRepository();
    private final FakeMembershipRepository membershipRepository = new FakeMembershipRepository();
    private final FakeOrganizationRepository organizationRepository = new FakeOrganizationRepository(membershipRepository);
    private final MembershipServiceImpl service = new MembershipServiceImpl(
            organizationRepository,
            membershipRepository,
            identityRepository,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private UUID organizationId;
    private UUID ownerId;
    private UUID adminId;
    private UUID targetId;

    /**
     * Creates one active Organization and known Identities for each test.
     */
    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        targetId = UUID.randomUUID();
        identityRepository.save(identity(ownerId, "owner"));
        identityRepository.save(identity(adminId, "admin"));
        identityRepository.save(identity(targetId, "target"));
        organizationRepository.save(Organization.create(
                organizationId, "Example Organization", "example-organization", NOW));
        membershipRepository.save(Membership.create(
                UUID.randomUUID(), organizationId, ownerId, MembershipRole.OWNER, NOW));
    }

    /**
     * Confirms a manager can add a member and receive profile data with the role.
     */
    @Test
    void addsMemberAndMapsIdentityProfile() {
        var member = service.add(new AddOrganizationMemberCommand(
                ownerId, organizationId, targetId, MembershipRole.MEMBER));

        assertEquals(targetId, member.identityId());
        assertEquals("target@example.com", member.email());
        assertEquals(MembershipRole.MEMBER, member.role());
    }

    /**
     * Confirms duplicate membership and ADMIN owner assignment are rejected.
     */
    @Test
    void rejectsDuplicateAndAdminOwnerOperations() {
        membershipRepository.save(Membership.create(
                UUID.randomUUID(), organizationId, adminId, MembershipRole.ADMIN, NOW));

        assertThrows(
                MembershipForbiddenException.class,
                () -> service.add(new AddOrganizationMemberCommand(
                        adminId, organizationId, targetId, MembershipRole.OWNER)));

        service.add(new AddOrganizationMemberCommand(ownerId, organizationId, targetId, MembershipRole.MEMBER));
        assertThrows(
                MembershipConflictException.class,
                () -> service.add(new AddOrganizationMemberCommand(
                        ownerId, organizationId, targetId, MembershipRole.MEMBER)));
    }

    /**
     * Confirms ADMIN can manage ordinary members but cannot change or remove an OWNER.
     */
    @Test
    void restrictsAdminOwnerOperations() {
        membershipRepository.save(Membership.create(
                UUID.randomUUID(), organizationId, adminId, MembershipRole.ADMIN, NOW));
        Membership ownerMembership = membershipRepository.findByOrganizationAndIdentity(organizationId, ownerId)
                .orElseThrow();

        assertThrows(
                MembershipForbiddenException.class,
                () -> service.update(new UpdateOrganizationMemberCommand(
                        adminId, organizationId, ownerMembership.id(), MembershipRole.ADMIN)));
        assertThrows(
                MembershipForbiddenException.class,
                () -> service.remove(new RemoveOrganizationMemberCommand(
                        adminId, organizationId, ownerMembership.id())));
    }

    /**
     * Confirms member listing is available to an ordinary member and defaults pagination.
     */
    @Test
    void listsMembersWithDefaults() {
        service.add(new AddOrganizationMemberCommand(ownerId, organizationId, targetId, MembershipRole.VIEWER));

        MemberPage page = service.list(new ListOrganizationMembersQuery(
                targetId, organizationId, null, null));

        assertEquals(2, page.totalElements());
        assertEquals(0, page.page());
        assertEquals(20, page.size());
    }

    private Identity identity(UUID id, String subject) {
        return Identity.create(
                id,
                new IdentityClaims("https://issuer.example", subject, subject + "@example.com", subject),
                NOW);
    }

    private static final class FakeIdentityRepository implements IdentityRepository {

        private final Map<UUID, Identity> identities = new HashMap<>();

        @Override
        public Optional<Identity> findByProviderAndSubject(String provider, String providerSubject) {
            return identities.values().stream()
                    .filter(identity -> identity.provider().equals(provider))
                    .filter(identity -> identity.providerSubject().equals(providerSubject))
                    .findFirst();
        }

        @Override
        public Optional<Identity> findById(UUID id) {
            return Optional.ofNullable(identities.get(id));
        }

        @Override
        public Identity save(Identity identity) {
            identities.put(identity.id(), identity);
            return identity;
        }
    }

    private static final class FakeMembershipRepository implements OrganizationMembershipRepository {

        private final Map<UUID, Membership> memberships = new HashMap<>();

        @Override
        public Optional<Membership> findByOrganizationAndId(UUID organizationId, UUID membershipId) {
            return Optional.ofNullable(memberships.get(membershipId))
                    .filter(membership -> membership.organizationId().equals(organizationId));
        }

        @Override
        public Optional<Membership> findByOrganizationAndIdentity(UUID organizationId, UUID identityId) {
            return memberships.values().stream()
                    .filter(membership -> membership.organizationId().equals(organizationId))
                    .filter(membership -> membership.identityId().equals(identityId))
                    .findFirst();
        }

        @Override
        public MembershipPage findByOrganization(ListMembershipsQuery query) {
            List<Membership> items = memberships.values().stream()
                    .filter(membership -> membership.organizationId().equals(query.organizationId()))
                    .toList();
            return new MembershipPage(items, query.page(), query.size(), items.size());
        }

        @Override
        public Membership save(Membership membership) {
            memberships.put(membership.id(), membership);
            return membership;
        }

        @Override
        public void delete(Membership membership) {
            memberships.remove(membership.id());
        }
    }

    private static final class FakeOrganizationRepository implements OrganizationRepository {

        private final FakeMembershipRepository membershipRepository;
        private final Map<UUID, Organization> organizations = new HashMap<>();

        private FakeOrganizationRepository(FakeMembershipRepository membershipRepository) {
            this.membershipRepository = membershipRepository;
        }

        @Override
        public boolean existsBySlug(String slug) {
            return organizations.values().stream().anyMatch(organization -> organization.slug().equals(slug));
        }

        @Override
        public Optional<Organization> findActiveById(UUID id) {
            return Optional.ofNullable(organizations.get(id)).filter(Organization::isActive);
        }

        @Override
        public OrganizationPage findActive(OrganizationQuery query) {
            return new OrganizationPage(List.of(), query.page(), query.size(), 0);
        }

        @Override
        public OrganizationPage findActiveForIdentity(UUID identityId, OrganizationQuery query) {
            return new OrganizationPage(List.of(), query.page(), query.size(), 0);
        }

        @Override
        public Optional<Organization> findActiveByIdForIdentity(UUID organizationId, UUID identityId) {
            return findActiveById(organizationId)
                    .filter(organization -> membershipRepository
                            .findByOrganizationAndIdentity(organization.id(), identityId)
                            .isPresent());
        }

        @Override
        public Organization save(Organization organization) {
            organizations.put(organization.id(), organization);
            return organization;
        }

        @Override
        public Organization saveWithOwner(Organization organization, Membership ownerMembership) {
            save(organization);
            membershipRepository.save(ownerMembership);
            return organization;
        }
    }
}

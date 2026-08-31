package dev.nexcraft.latch.controlplane.service.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.command.CreateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.command.UpdateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationNotFoundException;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationValidationException;
import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipForbiddenException;
import dev.nexcraft.latch.controlplane.core.membership.query.ListMembershipsQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.MembershipPage;
import dev.nexcraft.latch.controlplane.core.organization.query.ListOrganizationsQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
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
import org.junit.jupiter.api.Test;

/**
 * Verifies organization application rules without starting Quarkus.
 */
class OrganizationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");
    private static final UUID ACTOR = UUID.randomUUID();

    private final FakeMembershipRepository membershipRepository = new FakeMembershipRepository();
    private final FakeOrganizationRepository repository = new FakeOrganizationRepository(membershipRepository);
    private final OrganizationServiceImpl service = new OrganizationServiceImpl(
            repository,
            membershipRepository,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void createsNormalizedUniqueSlug() {
        Organization organization = service.create(ACTOR, new CreateOrganizationCommand("Acme Corporation"));

        assertEquals("acme-corporation", organization.slug());
        assertEquals(NOW, organization.createdAt());
        assertEquals(
                MembershipRole.OWNER,
                membershipRepository.findByOrganizationAndIdentity(organization.id(), ACTOR)
                        .orElseThrow()
                        .role());
    }

    @Test
    void addsNumericSuffixWhenSlugExists() {
        service.create(ACTOR, new CreateOrganizationCommand("Acme Corporation"));

        Organization organization = service.create(ACTOR, new CreateOrganizationCommand("Acme Corporation"));

        assertEquals("acme-corporation-2", organization.slug());
    }

    @Test
    void keepsSlugWhenNameChanges() {
        Organization created = service.create(ACTOR, new CreateOrganizationCommand("Acme Corporation"));

        Organization updated = service.update(
                ACTOR, new UpdateOrganizationCommand(created.id(), "Acme International"));

        assertEquals("Acme International", updated.name());
        assertEquals("acme-corporation", updated.slug());
    }

    @Test
    void softDeleteMakesOrganizationUnaddressable() {
        Organization created = service.create(ACTOR, new CreateOrganizationCommand("Acme Corporation"));

        service.delete(ACTOR, created.id());

        assertThrows(OrganizationNotFoundException.class, () -> service.get(ACTOR, created.id()));
        assertThrows(
                OrganizationNotFoundException.class,
                () -> service.update(ACTOR, new UpdateOrganizationCommand(created.id(), "Renamed")));
    }

    @Test
    void rejectsInvalidListParametersBeforeRepositoryCall() {
        assertThrows(
                OrganizationValidationException.class,
                () -> service.list(ACTOR, new ListOrganizationsQuery(null, -1, 20, "createdAt,desc")));
        assertThrows(
                OrganizationValidationException.class,
                () -> service.list(ACTOR, new ListOrganizationsQuery(null, 0, 101, "createdAt,desc")));
        assertThrows(
                OrganizationValidationException.class,
                () -> service.list(ACTOR, new ListOrganizationsQuery(null, 0, 20, "id,asc")));
        assertEquals(0, repository.findCalls);
    }

    @Test
    void usesListDefaults() {
        service.list(ACTOR, new ListOrganizationsQuery(null, null, null, null));

        assertEquals(0, repository.lastQuery.page());
        assertEquals(20, repository.lastQuery.size());
        assertEquals("createdAt", repository.lastQuery.sortField().value());
        assertEquals("desc", repository.lastQuery.direction().value());
    }

    @Test
    void rejectsNonManagerOrganizationMutation() {
        UUID viewer = UUID.randomUUID();
        Organization organization = service.create(ACTOR, new CreateOrganizationCommand("Managed Organization"));
        membershipRepository.save(Membership.create(
                UUID.randomUUID(), organization.id(), viewer, MembershipRole.VIEWER, NOW));

        assertThrows(
                MembershipForbiddenException.class,
                () -> service.update(viewer, new UpdateOrganizationCommand(organization.id(), "Renamed")));
    }

    private static final class FakeOrganizationRepository implements OrganizationRepository {

        private final Map<UUID, Organization> organizations = new HashMap<>();
        private final FakeMembershipRepository membershipRepository;
        private int findCalls;
        private OrganizationQuery lastQuery;

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
            findCalls++;
            lastQuery = query;
            return new OrganizationPage(java.util.List.of(), query.page(), query.size(), 0);
        }

        @Override
        public OrganizationPage findActiveForIdentity(UUID identityId, OrganizationQuery query) {
            findCalls++;
            lastQuery = query;
            long count = organizations.values().stream()
                    .filter(Organization::isActive)
                    .filter(organization -> membershipRepository
                            .findByOrganizationAndIdentity(organization.id(), identityId)
                            .isPresent())
                    .count();
            return new OrganizationPage(java.util.List.of(), query.page(), query.size(), count);
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
}

package dev.nexcraft.latch.controlplane.service.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.command.CreateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.command.UpdateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationNotFoundException;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationValidationException;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.ListOrganizationsQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies organization application rules without starting Quarkus.
 */
class OrganizationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");

    private final FakeOrganizationRepository repository = new FakeOrganizationRepository();
    private final OrganizationServiceImpl service = new OrganizationServiceImpl(
            repository,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void createsNormalizedUniqueSlug() {
        Organization organization = service.create(new CreateOrganizationCommand("Acme Corporation"));

        assertEquals("acme-corporation", organization.slug());
        assertEquals(NOW, organization.createdAt());
    }

    @Test
    void addsNumericSuffixWhenSlugExists() {
        service.create(new CreateOrganizationCommand("Acme Corporation"));

        Organization organization = service.create(new CreateOrganizationCommand("Acme Corporation"));

        assertEquals("acme-corporation-2", organization.slug());
    }

    @Test
    void keepsSlugWhenNameChanges() {
        Organization created = service.create(new CreateOrganizationCommand("Acme Corporation"));

        Organization updated = service.update(new UpdateOrganizationCommand(created.id(), "Acme International"));

        assertEquals("Acme International", updated.name());
        assertEquals("acme-corporation", updated.slug());
    }

    @Test
    void softDeleteMakesOrganizationUnaddressable() {
        Organization created = service.create(new CreateOrganizationCommand("Acme Corporation"));

        service.delete(created.id());

        assertThrows(OrganizationNotFoundException.class, () -> service.get(created.id()));
        assertThrows(
                OrganizationNotFoundException.class,
                () -> service.update(new UpdateOrganizationCommand(created.id(), "Renamed")));
    }

    @Test
    void rejectsInvalidListParametersBeforeRepositoryCall() {
        assertThrows(
                OrganizationValidationException.class,
                () -> service.list(new ListOrganizationsQuery(null, -1, 20, "createdAt,desc")));
        assertThrows(
                OrganizationValidationException.class,
                () -> service.list(new ListOrganizationsQuery(null, 0, 101, "createdAt,desc")));
        assertThrows(
                OrganizationValidationException.class,
                () -> service.list(new ListOrganizationsQuery(null, 0, 20, "id,asc")));
        assertEquals(0, repository.findCalls);
    }

    @Test
    void usesListDefaults() {
        service.list(new ListOrganizationsQuery(null, null, null, null));

        assertEquals(0, repository.lastQuery.page());
        assertEquals(20, repository.lastQuery.size());
        assertEquals("createdAt", repository.lastQuery.sortField().value());
        assertEquals("desc", repository.lastQuery.direction().value());
    }

    private static final class FakeOrganizationRepository implements OrganizationRepository {

        private final Map<UUID, Organization> organizations = new HashMap<>();
        private int findCalls;
        private OrganizationQuery lastQuery;

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
        public Organization save(Organization organization) {
            organizations.put(organization.id(), organization);
            return organization;
        }
    }
}

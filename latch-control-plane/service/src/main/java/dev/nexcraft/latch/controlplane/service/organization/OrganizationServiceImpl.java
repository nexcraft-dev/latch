package dev.nexcraft.latch.controlplane.service.organization;

import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipForbiddenException;
import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.command.CreateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.command.UpdateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationNotFoundException;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationValidationException;
import dev.nexcraft.latch.controlplane.core.organization.query.ListOrganizationsQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationSortField;
import dev.nexcraft.latch.controlplane.core.organization.query.SortDirection;
import dev.nexcraft.latch.controlplane.core.organization.service.OrganizationService;
import dev.nexcraft.latch.controlplane.repository.membership.OrganizationMembershipRepository;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Framework-free implementation of the organization service contract.
 */
public final class OrganizationServiceImpl implements OrganizationService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT = "createdAt,desc";

    private final OrganizationRepository repository;
    private final OrganizationMembershipRepository membershipRepository;
    private final Clock clock;
    private final SlugGenerator slugGenerator;

    /**
     * Creates the organization service implementation.
     *
     * @param repository organization persistence port
     * @param membershipRepository membership persistence port
     * @param clock clock used for domain timestamps
     */
    public OrganizationServiceImpl(
            OrganizationRepository repository,
            OrganizationMembershipRepository membershipRepository,
            Clock clock) {
        this.repository = repository;
        this.membershipRepository = membershipRepository;
        this.clock = clock;
        this.slugGenerator = new SlugGenerator();
    }

    @Override
    public Organization create(UUID actorIdentityId, CreateOrganizationCommand command) {
        requireActor(actorIdentityId);
        validateCommand(command);
        String slug = slugGenerator.generateUnique(command.name(), repository::existsBySlug);
        Instant now = Instant.now(clock);
        Organization organization = Organization.create(UUID.randomUUID(), command.name(), slug, now);
        Membership ownerMembership = Membership.create(
                UUID.randomUUID(), organization.id(), actorIdentityId, MembershipRole.OWNER, now);
        return repository.saveWithOwner(organization, ownerMembership);
    }

    @Override
    public OrganizationPage list(UUID actorIdentityId, ListOrganizationsQuery query) {
        requireActor(actorIdentityId);
        if (query == null) {
            throw new OrganizationValidationException("Organization list query is required");
        }
        int page = query.page() == null ? DEFAULT_PAGE : query.page();
        int size = query.size() == null ? DEFAULT_SIZE : query.size();
        validatePage(page, size);
        String sort = query.sort() == null || query.sort().isBlank() ? DEFAULT_SORT : query.sort();
        String[] sortParts = sort.split(",", -1);
        if (sortParts.length != 2) {
            throw new OrganizationValidationException("sort must use field,direction format");
        }
        OrganizationQuery repositoryQuery = new OrganizationQuery(
                normalizeSearch(query.search()),
                page,
                size,
                OrganizationSortField.parse(sortParts[0]),
                SortDirection.parse(sortParts[1]));
        return repository.findActiveForIdentity(actorIdentityId, repositoryQuery);
    }

    @Override
    public Organization get(UUID actorIdentityId, UUID id) {
        requireActor(actorIdentityId);
        requireOrganizationId(id);
        return repository.findActiveByIdForIdentity(id, actorIdentityId)
                .orElseThrow(() -> new OrganizationNotFoundException(id));
    }

    @Override
    public Organization update(UUID actorIdentityId, UpdateOrganizationCommand command) {
        requireActor(actorIdentityId);
        if (command == null || command.id() == null) {
            throw new OrganizationValidationException("Organization id is required");
        }
        Organization organization = get(actorIdentityId, command.id());
        requireManager(actorIdentityId, command.id());
        return repository.save(organization.rename(command.name(), Instant.now(clock)));
    }

    @Override
    public void delete(UUID actorIdentityId, UUID id) {
        requireActor(actorIdentityId);
        Organization organization = get(actorIdentityId, id);
        requireManager(actorIdentityId, id);
        repository.save(organization.markDeleted(Instant.now(clock)));
    }

    private void requireActor(UUID actorIdentityId) {
        if (actorIdentityId == null) {
            throw new OrganizationValidationException("Authenticated Identity is required");
        }
    }

    private void requireOrganizationId(UUID id) {
        if (id == null) {
            throw new OrganizationValidationException("Organization id is required");
        }
    }

    private void requireManager(UUID actorIdentityId, UUID organizationId) {
        requireOrganizationId(organizationId);
        Membership membership = membershipRepository.findByOrganizationAndIdentity(organizationId, actorIdentityId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        if (membership.role() != MembershipRole.OWNER && membership.role() != MembershipRole.ADMIN) {
            throw new MembershipForbiddenException("Organization management requires OWNER or ADMIN role");
        }
    }

    private void validateCommand(CreateOrganizationCommand command) {
        if (command == null || command.name() == null || command.name().isBlank()) {
            throw new OrganizationValidationException("Organization name must not be blank");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new OrganizationValidationException("page must be greater than or equal to zero");
        }
        if (size < 1 || size > 100) {
            throw new OrganizationValidationException("size must be between 1 and 100");
        }
    }

    private String normalizeSearch(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }
}

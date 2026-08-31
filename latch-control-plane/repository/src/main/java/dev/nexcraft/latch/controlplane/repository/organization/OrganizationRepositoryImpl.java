package dev.nexcraft.latch.controlplane.repository.organization;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.OrganizationStatus;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationSortField;
import dev.nexcraft.latch.controlplane.core.organization.query.SortDirection;
import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.repository.membership.persistence.OrganizationMembershipEntityMapper;
import dev.nexcraft.latch.controlplane.repository.membership.persistence.OrganizationMembershipPanacheRepository;
import dev.nexcraft.latch.controlplane.repository.organization.persistence.OrganizationEntity;
import dev.nexcraft.latch.controlplane.repository.organization.persistence.OrganizationEntityMapper;
import dev.nexcraft.latch.controlplane.repository.organization.persistence.OrganizationPanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Panache-backed organization repository.
 */
@ApplicationScoped
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private final OrganizationPanacheRepository repository;
    private final OrganizationEntityMapper mapper;
    private final OrganizationMembershipPanacheRepository membershipRepository;
    private final OrganizationMembershipEntityMapper membershipMapper;

    /**
     * Creates the organization repository.
     *
     * @param repository Panache access object
     * @param membershipRepository membership Panache access object
     */
    public OrganizationRepositoryImpl(
            OrganizationPanacheRepository repository,
            OrganizationMembershipPanacheRepository membershipRepository) {
        this.repository = repository;
        this.mapper = new OrganizationEntityMapper();
        this.membershipRepository = membershipRepository;
        this.membershipMapper = new OrganizationMembershipEntityMapper();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.count("slug", slug) > 0;
    }

    @Override
    public Optional<Organization> findActiveById(UUID id) {
        return repository.find("status = ?1 and id = ?2", OrganizationStatus.ACTIVE, id)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public OrganizationPage findActive(OrganizationQuery query) {
        List<Object> parameters = new ArrayList<>();
        parameters.add(OrganizationStatus.ACTIVE);
        return findPage("status = ?1", parameters, query);
    }

    @Override
    public OrganizationPage findActiveForIdentity(UUID identityId, OrganizationQuery query) {
        List<Object> parameters = new ArrayList<>();
        parameters.add(OrganizationStatus.ACTIVE);
        parameters.add(identityId);
        String where = "status = ?1 and id in (select membership.organizationId "
                + "from OrganizationMembershipEntity membership where membership.identityId = ?2)";
        return findPage(where, parameters, query);
    }

    @Override
    public Optional<Organization> findActiveByIdForIdentity(UUID organizationId, UUID identityId) {
        String where = "status = ?1 and id = ?2 and id in (select membership.organizationId "
                + "from OrganizationMembershipEntity membership where membership.identityId = ?3)";
        return repository.find(where, OrganizationStatus.ACTIVE, organizationId, identityId)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    private OrganizationPage findPage(String baseWhere, List<Object> parameters, OrganizationQuery query) {
        String where = baseWhere;
        if (query.search() != null) {
            int searchParameter = parameters.size() + 1;
            where += " and (lower(name) like ?" + searchParameter + " or lower(slug) like ?" + searchParameter + ")";
            parameters.add("%" + query.search().toLowerCase(Locale.ROOT) + "%");
        }
        Object[] parameterArray = parameters.toArray();
        long totalElements = repository.count(where, parameterArray);
        PanacheQuery<OrganizationEntity> pageQuery = repository.find(
                where,
                sortFor(query),
                parameterArray);
        List<Organization> items = pageQuery.page(Page.of(query.page(), query.size())).list()
                .stream()
                .map(mapper::toDomain)
                .toList();
        return new OrganizationPage(items, query.page(), query.size(), totalElements);
    }

    @Override
    @Transactional
    public Organization save(Organization organization) {
        OrganizationEntity entity = repository.findById(organization.id());
        if (entity == null) {
            entity = mapper.toEntity(organization);
            repository.persist(entity);
        } else {
            entity.apply(organization);
        }
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Organization saveWithOwner(Organization organization, Membership ownerMembership) {
        OrganizationEntity entity = mapper.toEntity(organization);
        repository.persist(entity);
        membershipRepository.persist(membershipMapper.toEntity(ownerMembership));
        return mapper.toDomain(entity);
    }

    private Sort sortFor(OrganizationQuery query) {
        String property = propertyFor(query.sortField());
        Sort sort = Sort.by(property);
        if (query.direction() == SortDirection.ASC) {
            sort.ascending();
        } else {
            sort.descending();
        }
        sort.and("id", Sort.Direction.Ascending);
        return sort;
    }

    private String propertyFor(OrganizationSortField field) {
        return switch (field) {
            case NAME -> "name";
            case SLUG -> "slug";
            case CREATED_AT -> "createdAt";
            case UPDATED_AT -> "updatedAt";
        };
    }
}

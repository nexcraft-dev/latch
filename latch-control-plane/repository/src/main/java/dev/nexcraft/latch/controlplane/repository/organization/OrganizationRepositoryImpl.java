package dev.nexcraft.latch.controlplane.repository.organization;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.OrganizationStatus;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationSortField;
import dev.nexcraft.latch.controlplane.core.organization.query.SortDirection;
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

    /**
     * Creates the organization repository.
     *
     * @param repository Panache access object
     */
    public OrganizationRepositoryImpl(OrganizationPanacheRepository repository) {
        this.repository = repository;
        this.mapper = new OrganizationEntityMapper();
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
        String where = "status = ?1";
        if (query.search() != null) {
            where += " and (lower(name) like ?2 or lower(slug) like ?2)";
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

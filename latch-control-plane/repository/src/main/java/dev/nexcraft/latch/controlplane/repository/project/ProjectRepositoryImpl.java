package dev.nexcraft.latch.controlplane.repository.project;

import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.ProjectStatus;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectPage;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectSortDirection;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectSortField;
import dev.nexcraft.latch.controlplane.repository.project.persistence.ProjectEntity;
import dev.nexcraft.latch.controlplane.repository.project.persistence.ProjectEntityMapper;
import dev.nexcraft.latch.controlplane.repository.project.persistence.ProjectPanacheRepository;
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
 * Panache-backed Project repository.
 */
@ApplicationScoped
public class ProjectRepositoryImpl implements ProjectRepository {

    private final ProjectPanacheRepository repository;
    private final ProjectEntityMapper mapper;

    /**
     * Creates the Project repository.
     *
     * @param repository Panache access object
     */
    public ProjectRepositoryImpl(ProjectPanacheRepository repository) {
        this.repository = repository;
        this.mapper = new ProjectEntityMapper();
    }

    @Override
    public boolean existsByOrganizationAndKey(UUID organizationId, String key) {
        return repository.count("organizationId = ?1 and key = ?2", organizationId, key) > 0;
    }

    @Override
    public Optional<Project> findActiveByOrganizationAndId(UUID organizationId, UUID projectId) {
        return repository.find(
                        "status = ?1 and organizationId = ?2 and id = ?3",
                        ProjectStatus.ACTIVE,
                        organizationId,
                        projectId)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public ProjectPage findActiveByOrganization(UUID organizationId, ProjectQuery query) {
        List<Object> parameters = new ArrayList<>();
        parameters.add(ProjectStatus.ACTIVE);
        parameters.add(organizationId);
        String where = "status = ?1 and organizationId = ?2";
        if (query.search() != null) {
            int searchParameter = parameters.size() + 1;
            where += " and (lower(name) like ?" + searchParameter
                    + " or lower(key) like ?" + searchParameter
                    + " or lower(description) like ?" + searchParameter + ")";
            parameters.add("%" + query.search().toLowerCase(Locale.ROOT) + "%");
        }
        Object[] parameterArray = parameters.toArray();
        long totalElements = repository.count(where, parameterArray);
        PanacheQuery<ProjectEntity> pageQuery = repository.find(
                where,
                sortFor(query),
                parameterArray);
        List<Project> items = pageQuery.page(Page.of(query.page(), query.size())).list()
                .stream()
                .map(mapper::toDomain)
                .toList();
        return new ProjectPage(items, query.page(), query.size(), totalElements);
    }

    @Override
    @Transactional
    public Project save(Project project) {
        ProjectEntity entity = repository.findById(project.id());
        if (entity == null) {
            entity = mapper.toEntity(project);
            repository.persist(entity);
        } else {
            entity.apply(project);
        }
        return mapper.toDomain(entity);
    }

    private Sort sortFor(ProjectQuery query) {
        String property = propertyFor(query.sortField());
        Sort sort = Sort.by(property);
        if (query.direction() == ProjectSortDirection.ASC) {
            sort.ascending();
        } else {
            sort.descending();
        }
        sort.and("id", Sort.Direction.Ascending);
        return sort;
    }

    private String propertyFor(ProjectSortField field) {
        return switch (field) {
            case NAME -> "name";
            case KEY -> "key";
            case CREATED_AT -> "createdAt";
            case UPDATED_AT -> "updatedAt";
        };
    }
}

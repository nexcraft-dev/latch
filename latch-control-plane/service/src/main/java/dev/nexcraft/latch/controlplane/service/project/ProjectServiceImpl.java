package dev.nexcraft.latch.controlplane.service.project;

import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipForbiddenException;
import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.command.CreateProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.command.DeleteProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.command.UpdateProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.error.ProjectKeyConflictException;
import dev.nexcraft.latch.controlplane.core.project.error.ProjectNotFoundException;
import dev.nexcraft.latch.controlplane.core.project.error.ProjectValidationException;
import dev.nexcraft.latch.controlplane.core.project.query.GetProjectQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ListProjectsQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectPage;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectSortDirection;
import dev.nexcraft.latch.controlplane.core.project.query.ProjectSortField;
import dev.nexcraft.latch.controlplane.core.project.service.ProjectService;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationNotFoundException;
import dev.nexcraft.latch.controlplane.repository.membership.OrganizationMembershipRepository;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import dev.nexcraft.latch.controlplane.repository.project.ProjectRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Framework-free implementation of the Organization-scoped Project service.
 */
public final class ProjectServiceImpl implements ProjectService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT = "createdAt,desc";

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final Clock clock;

    /**
     * Creates the Project service implementation.
     *
     * @param projectRepository Project persistence boundary
     * @param organizationRepository Organization persistence boundary
     * @param membershipRepository Organization membership persistence boundary
     * @param clock clock used for Project timestamps
     */
    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            OrganizationRepository organizationRepository,
            OrganizationMembershipRepository membershipRepository,
            Clock clock) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.clock = clock;
    }

    @Override
    public Project create(CreateProjectCommand command) {
        validateCreate(command);
        requirePermission(command.actorIdentityId(), command.organizationId(), ProjectPermission.CREATE);
        if (projectRepository.existsByOrganizationAndKey(command.organizationId(), command.key().trim())) {
            throw new ProjectKeyConflictException(command.key().trim());
        }
        Instant now = Instant.now(clock);
        Project project = Project.create(
                UUID.randomUUID(),
                command.organizationId(),
                command.name(),
                command.key(),
                command.description(),
                now);
        return projectRepository.save(project);
    }

    @Override
    public ProjectPage list(ListProjectsQuery query) {
        ListInput input = listInput(query);
        requireAccess(input.actorIdentityId(), input.organizationId());
        return projectRepository.findActiveByOrganization(
                input.organizationId(),
                new ProjectQuery(
                        input.search(),
                        input.page(),
                        input.size(),
                        input.sortField(),
                        input.direction()));
    }

    @Override
    public Project get(GetProjectQuery query) {
        validateGet(query);
        requireAccess(query.actorIdentityId(), query.organizationId());
        return findProject(query.organizationId(), query.projectId());
    }

    @Override
    public Project update(UpdateProjectCommand command) {
        validateUpdate(command);
        requireAccess(command.actorIdentityId(), command.organizationId());
        Project project = findProject(command.organizationId(), command.projectId());
        requirePermission(command.actorIdentityId(), command.organizationId(), ProjectPermission.UPDATE);
        return projectRepository.save(project.update(
                command.name(),
                command.description() == null ? project.description() : command.description(),
                Instant.now(clock)));
    }

    @Override
    public void delete(DeleteProjectCommand command) {
        validateDelete(command);
        requireAccess(command.actorIdentityId(), command.organizationId());
        Project project = findProject(command.organizationId(), command.projectId());
        requirePermission(command.actorIdentityId(), command.organizationId(), ProjectPermission.DELETE);
        projectRepository.save(project.markDeleted(Instant.now(clock)));
    }

    private Project findProject(UUID organizationId, UUID projectId) {
        return projectRepository.findActiveByOrganizationAndId(organizationId, projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private Membership requireAccess(UUID actorIdentityId, UUID organizationId) {
        requireIdentifiers(actorIdentityId, organizationId);
        organizationRepository.findActiveByIdForIdentity(organizationId, actorIdentityId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        return membershipRepository.findByOrganizationAndIdentity(organizationId, actorIdentityId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }

    private void requirePermission(UUID actorIdentityId, UUID organizationId, ProjectPermission permission) {
        Membership membership = requireAccess(actorIdentityId, organizationId);
        if (!permission.isAllowed(membership.role())) {
            throw new MembershipForbiddenException(
                    "Organization membership role does not allow this Project operation");
        }
    }

    private ListInput listInput(ListProjectsQuery query) {
        if (query == null) {
            throw new ProjectValidationException("Project list query is required");
        }
        requireIdentifiers(query.actorIdentityId(), query.organizationId());
        int page = query.page() == null ? DEFAULT_PAGE : query.page();
        int size = query.size() == null ? DEFAULT_SIZE : query.size();
        validatePage(page, size);
        String sort = query.sort() == null || query.sort().isBlank() ? DEFAULT_SORT : query.sort();
        String[] sortParts = sort.split(",", -1);
        if (sortParts.length != 2) {
            throw new ProjectValidationException("sort must use field,direction format");
        }
        return new ListInput(
                query.actorIdentityId(),
                query.organizationId(),
                normalizeSearch(query.search()),
                page,
                size,
                ProjectSortField.parse(sortParts[0]),
                ProjectSortDirection.parse(sortParts[1]));
    }

    private void validateCreate(CreateProjectCommand command) {
        if (command == null) {
            throw new ProjectValidationException("Create Project command is required");
        }
        requireIdentifiers(command.actorIdentityId(), command.organizationId());
        if (command.name() == null || command.name().isBlank()) {
            throw new ProjectValidationException("Project name must not be blank");
        }
        if (command.key() == null || command.key().isBlank()) {
            throw new ProjectValidationException("Project key must not be blank");
        }
    }

    private void validateGet(GetProjectQuery query) {
        if (query == null) {
            throw new ProjectValidationException("Get Project query is required");
        }
        requireIdentifiers(query.actorIdentityId(), query.organizationId());
        if (query.projectId() == null) {
            throw new ProjectValidationException("Project id is required");
        }
    }

    private void validateUpdate(UpdateProjectCommand command) {
        if (command == null) {
            throw new ProjectValidationException("Update Project command is required");
        }
        requireIdentifiers(command.actorIdentityId(), command.organizationId());
        if (command.projectId() == null) {
            throw new ProjectValidationException("Project id is required");
        }
        if (command.name() == null || command.name().isBlank()) {
            throw new ProjectValidationException("Project name must not be blank");
        }
    }

    private void validateDelete(DeleteProjectCommand command) {
        if (command == null) {
            throw new ProjectValidationException("Delete Project command is required");
        }
        requireIdentifiers(command.actorIdentityId(), command.organizationId());
        if (command.projectId() == null) {
            throw new ProjectValidationException("Project id is required");
        }
    }

    private void requireIdentifiers(UUID actorIdentityId, UUID organizationId) {
        if (actorIdentityId == null) {
            throw new ProjectValidationException("Authenticated Identity is required");
        }
        if (organizationId == null) {
            throw new ProjectValidationException("Organization id is required");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new ProjectValidationException("page must be greater than or equal to zero");
        }
        if (size < 1 || size > 100) {
            throw new ProjectValidationException("size must be between 1 and 100");
        }
    }

    private String normalizeSearch(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }

    private enum ProjectPermission {
        CREATE {
            @Override
            boolean isAllowed(MembershipRole role) {
                return role == MembershipRole.OWNER
                        || role == MembershipRole.ADMIN
                        || role == MembershipRole.MEMBER;
            }
        },
        UPDATE {
            @Override
            boolean isAllowed(MembershipRole role) {
                return role == MembershipRole.OWNER
                        || role == MembershipRole.ADMIN
                        || role == MembershipRole.MEMBER;
            }
        },
        DELETE {
            @Override
            boolean isAllowed(MembershipRole role) {
                return role == MembershipRole.OWNER || role == MembershipRole.ADMIN;
            }
        };

        abstract boolean isAllowed(MembershipRole role);
    }

    private record ListInput(
            UUID actorIdentityId,
            UUID organizationId,
            String search,
            int page,
            int size,
            ProjectSortField sortField,
            ProjectSortDirection direction) {
    }
}

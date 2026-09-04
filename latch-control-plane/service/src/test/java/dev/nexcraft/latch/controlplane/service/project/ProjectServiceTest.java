package dev.nexcraft.latch.controlplane.service.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipForbiddenException;
import dev.nexcraft.latch.controlplane.core.membership.query.ListMembershipsQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.MembershipPage;
import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationQuery;
import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.ProjectStatus;
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
import dev.nexcraft.latch.controlplane.repository.membership.OrganizationMembershipRepository;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import dev.nexcraft.latch.controlplane.repository.project.ProjectRepository;
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
 * Verifies Project application rules without starting Quarkus.
 */
class ProjectServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T10:00:00Z");

    private final FakeMembershipRepository membershipRepository = new FakeMembershipRepository();
    private final FakeOrganizationRepository organizationRepository = new FakeOrganizationRepository(membershipRepository);
    private final FakeProjectRepository projectRepository = new FakeProjectRepository();
    private final ProjectServiceImpl service = new ProjectServiceImpl(
            projectRepository,
            organizationRepository,
            membershipRepository,
            Clock.fixed(NOW, ZoneOffset.UTC));

    /**
     * Confirms a MEMBER can create and update a Project but cannot delete it.
     */
    @Test
    void appliesMemberCreateUpdateDeletePermissions() {
        UUID actor = UUID.randomUUID();
        UUID organizationId = addOrganization(actor, MembershipRole.MEMBER);

        Project created = service.create(new CreateProjectCommand(
                actor, organizationId, "Checkout", "checkout", "Initial"));
        Project updated = service.update(new UpdateProjectCommand(
                actor, organizationId, created.id(), "Checkout Platform", "Updated"));

        assertEquals("checkout", updated.key());
        assertEquals("Checkout Platform", updated.name());
        assertThrows(
                MembershipForbiddenException.class,
                () -> service.delete(new DeleteProjectCommand(actor, organizationId, created.id())));
        assertEquals(ProjectStatus.ACTIVE, projectRepository.projects.get(created.id()).status());
    }

    /**
     * Confirms each membership role can read active Projects.
     */
    @Test
    void allowsAllMembershipRolesToRead() {
        for (MembershipRole role : MembershipRole.values()) {
            UUID actor = UUID.randomUUID();
            UUID organizationId = addOrganization(actor, role);
            Project project = projectRepository.save(project(
                    organizationId, "Project " + role, "project-" + role.name().toLowerCase()));

            assertEquals(
                    project.id(),
                    service.get(new GetProjectQuery(actor, organizationId, project.id())).id());
            assertEquals(
                    1,
                    service.list(new ListProjectsQuery(actor, organizationId, null, null, null, null))
                            .totalElements());
        }
    }

    /**
     * Confirms OWNER and ADMIN can delete Projects and the operation is soft.
     */
    @Test
    void allowsOwnerAndAdminToSoftDelete() {
        for (MembershipRole role : List.of(MembershipRole.OWNER, MembershipRole.ADMIN)) {
            UUID actor = UUID.randomUUID();
            UUID organizationId = addOrganization(actor, role);
            Project project = projectRepository.save(project(organizationId, "Project", "project-" + role.name().toLowerCase()));

            service.delete(new DeleteProjectCommand(actor, organizationId, project.id()));

            assertEquals(ProjectStatus.DELETED, projectRepository.projects.get(project.id()).status());
            assertThrows(
                    ProjectNotFoundException.class,
                    () -> service.get(new GetProjectQuery(actor, organizationId, project.id())));
        }
    }

    /**
     * Confirms VIEWER cannot create, update, or delete Projects.
     */
    @Test
    void rejectsViewerMutations() {
        UUID actor = UUID.randomUUID();
        UUID organizationId = addOrganization(actor, MembershipRole.VIEWER);
        Project existing = projectRepository.save(project(organizationId, "Existing", "existing"));

        assertThrows(
                MembershipForbiddenException.class,
                () -> service.create(new CreateProjectCommand(
                        actor, organizationId, "New", "new", null)));
        assertThrows(
                MembershipForbiddenException.class,
                () -> service.update(new UpdateProjectCommand(
                        actor, organizationId, existing.id(), "Changed", null)));
        assertThrows(
                MembershipForbiddenException.class,
                () -> service.delete(new DeleteProjectCommand(actor, organizationId, existing.id())));
    }

    /**
     * Confirms an omitted description is preserved and an empty value clears it.
     */
    @Test
    void preservesDescriptionWhenUpdateOmitsIt() {
        UUID actor = UUID.randomUUID();
        UUID organizationId = addOrganization(actor, MembershipRole.OWNER);
        Project existing = projectRepository.save(Project.create(
                UUID.randomUUID(), organizationId, "Existing", "existing", "Keep this", NOW));

        Project preserved = service.update(new UpdateProjectCommand(
                actor, organizationId, existing.id(), "Renamed", null));
        Project cleared = service.update(new UpdateProjectCommand(
                actor, organizationId, existing.id(), "Renamed again", ""));

        assertEquals("Keep this", preserved.description());
        assertNull(cleared.description());
    }

    /**
     * Confirms a nested path cannot access a Project from another Organization.
     */
    @Test
    void rejectsCrossOrganizationProjectAccess() {
        UUID actor = UUID.randomUUID();
        UUID organizationId = addOrganization(actor, MembershipRole.OWNER);
        UUID otherOrganizationId = UUID.randomUUID();
        Project project = projectRepository.save(project(otherOrganizationId, "Other", "other"));

        assertThrows(
                ProjectNotFoundException.class,
                () -> service.get(new GetProjectQuery(actor, organizationId, project.id())));
    }

    /**
     * Confirms duplicate keys and invalid list values are rejected before persistence.
     */
    @Test
    void rejectsDuplicateKeysAndInvalidListValues() {
        UUID actor = UUID.randomUUID();
        UUID organizationId = addOrganization(actor, MembershipRole.OWNER);
        service.create(new CreateProjectCommand(actor, organizationId, "First", "same-key", null));

        assertThrows(
                ProjectKeyConflictException.class,
                () -> service.create(new CreateProjectCommand(actor, organizationId, "Second", "same-key", null)));
        assertThrows(
                ProjectValidationException.class,
                () -> service.list(new ListProjectsQuery(actor, organizationId, null, -1, 20, "name,asc")));
        assertThrows(
                ProjectValidationException.class,
                () -> service.list(new ListProjectsQuery(actor, organizationId, null, 0, 20, "id,asc")));
    }

    private UUID addOrganization(UUID actor, MembershipRole role) {
        UUID organizationId = UUID.randomUUID();
        organizationRepository.save(Organization.create(
                organizationId,
                "Organization " + organizationId,
                "organization-" + organizationId,
                NOW));
        membershipRepository.save(Membership.create(
                UUID.randomUUID(), organizationId, actor, role, NOW));
        return organizationId;
    }

    private Project project(UUID organizationId, String name, String key) {
        return Project.create(UUID.randomUUID(), organizationId, name, key, null, NOW);
    }

    private static final class FakeProjectRepository implements ProjectRepository {

        private final Map<UUID, Project> projects = new HashMap<>();

        @Override
        public boolean existsByOrganizationAndKey(UUID organizationId, String key) {
            return projects.values().stream()
                    .anyMatch(project -> project.organizationId().equals(organizationId) && project.key().equals(key));
        }

        @Override
        public Optional<Project> findActiveByOrganizationAndId(UUID organizationId, UUID projectId) {
            return Optional.ofNullable(projects.get(projectId))
                    .filter(Project::isActive)
                    .filter(project -> project.organizationId().equals(organizationId));
        }

        @Override
        public ProjectPage findActiveByOrganization(UUID organizationId, ProjectQuery query) {
            List<Project> items = projects.values().stream()
                    .filter(Project::isActive)
                    .filter(project -> project.organizationId().equals(organizationId))
                    .toList();
            return new ProjectPage(items, query.page(), query.size(), items.size());
        }

        @Override
        public Project save(Project project) {
            projects.put(project.id(), project);
            return project;
        }
    }

    private static final class FakeOrganizationRepository implements OrganizationRepository {

        private final Map<UUID, Organization> organizations = new HashMap<>();
        private final FakeMembershipRepository membershipRepository;

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
            return save(organization);
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
            return new MembershipPage(List.of(), query.page(), query.size(), 0);
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

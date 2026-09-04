package dev.nexcraft.latch.controlplane.web.organization;

import dev.nexcraft.latch.controlplane.core.identity.service.IdentityService;
import dev.nexcraft.latch.controlplane.core.membership.service.MembershipService;
import dev.nexcraft.latch.controlplane.core.organization.service.OrganizationService;
import dev.nexcraft.latch.controlplane.core.project.service.ProjectService;
import dev.nexcraft.latch.controlplane.repository.identity.IdentityRepository;
import dev.nexcraft.latch.controlplane.repository.membership.OrganizationMembershipRepository;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import dev.nexcraft.latch.controlplane.repository.project.ProjectRepository;
import dev.nexcraft.latch.controlplane.service.identity.IdentityServiceImpl;
import dev.nexcraft.latch.controlplane.service.membership.MembershipServiceImpl;
import dev.nexcraft.latch.controlplane.service.organization.OrganizationServiceImpl;
import dev.nexcraft.latch.controlplane.service.project.ProjectServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;

/**
 * Wires the framework-free organization service into the Quarkus application.
 */
@ApplicationScoped
public class OrganizationServiceProducer {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final IdentityRepository identityRepository;
    private final ProjectRepository projectRepository;

    /**
     * Creates the service producer.
     *
     * @param organizationRepository organization repository
     * @param membershipRepository membership repository
     * @param identityRepository Identity repository
     */
    public OrganizationServiceProducer(
            OrganizationRepository organizationRepository,
            OrganizationMembershipRepository membershipRepository,
            IdentityRepository identityRepository,
            ProjectRepository projectRepository) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.identityRepository = identityRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Produces an organization service with the system UTC clock.
     *
     * @return application service
     */
    @Produces
    @ApplicationScoped
    public OrganizationService organizationService() {
        return new OrganizationServiceImpl(organizationRepository, membershipRepository, Clock.systemUTC());
    }

    /**
     * Produces the Identity resolution service with the system UTC clock.
     *
     * @return application service
     */
    @Produces
    @ApplicationScoped
    public IdentityService identityService() {
        return new IdentityServiceImpl(identityRepository, Clock.systemUTC());
    }

    /**
     * Produces the membership service with the system UTC clock.
     *
     * @return application service
     */
    @Produces
    @ApplicationScoped
    public MembershipService membershipService() {
        return new MembershipServiceImpl(
                organizationRepository,
                membershipRepository,
                identityRepository,
                Clock.systemUTC());
    }

    /**
     * Produces a Project service with the system UTC clock.
     *
     * @return application service
     */
    @Produces
    @ApplicationScoped
    public ProjectService projectService() {
        return new ProjectServiceImpl(
                projectRepository,
                organizationRepository,
                membershipRepository,
                Clock.systemUTC());
    }
}

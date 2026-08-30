package dev.nexcraft.latch.controlplane.web.organization;

import dev.nexcraft.latch.controlplane.core.organization.service.OrganizationService;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import dev.nexcraft.latch.controlplane.service.organization.OrganizationServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;

/**
 * Wires the framework-free organization service into the Quarkus application.
 */
@ApplicationScoped
public class OrganizationServiceProducer {

    private final OrganizationRepository repository;

    /**
     * Creates the service producer.
     *
     * @param repository organization repository port
     */
    public OrganizationServiceProducer(OrganizationRepository repository) {
        this.repository = repository;
    }

    /**
     * Produces an organization service with the system UTC clock.
     *
     * @return application service
     */
    @Produces
    @ApplicationScoped
    public OrganizationService organizationService() {
        return new OrganizationServiceImpl(repository, Clock.systemUTC());
    }
}

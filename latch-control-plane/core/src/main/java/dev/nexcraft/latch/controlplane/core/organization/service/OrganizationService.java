package dev.nexcraft.latch.controlplane.core.organization.service;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.command.CreateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.command.UpdateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.query.ListOrganizationsQuery;
import dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage;
import java.util.UUID;

/**
 * Application service contract for organization use cases.
 *
 * <p>The contract is framework-independent. Web adapters map their transport
 * concerns to the handwritten models and commands in {@code core} without
 * exposing persistence types.</p>
 */
public interface OrganizationService {

    /**
     * Creates an active organization.
     *
     * @param actorIdentityId authenticated creator
     * @param command creation input
     * @return created organization
     */
    Organization create(UUID actorIdentityId, CreateOrganizationCommand command);

    /**
     * Lists active organizations.
     *
     * @param actorIdentityId authenticated caller
     * @param query listing input
     * @return active organization page
     */
    OrganizationPage list(UUID actorIdentityId, ListOrganizationsQuery query);

    /**
     * Gets an active organization.
     *
     * @param actorIdentityId authenticated caller
     * @param id organization identifier
     * @return active organization
     */
    Organization get(UUID actorIdentityId, UUID id);

    /**
     * Renames an active organization without changing its slug.
     *
     * @param actorIdentityId authenticated caller
     * @param command update input
     * @return updated organization
     */
    Organization update(UUID actorIdentityId, UpdateOrganizationCommand command);

    /**
     * Soft-deletes an active organization.
     *
     * @param actorIdentityId authenticated caller
     * @param id organization identifier
     */
    void delete(UUID actorIdentityId, UUID id);
}

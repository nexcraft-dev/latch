package dev.nexcraft.latch.controlplane.web.organization;

import dev.nexcraft.latch.controlplane.core.organization.Organization;
import dev.nexcraft.latch.controlplane.core.organization.command.CreateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.command.UpdateOrganizationCommand;
import dev.nexcraft.latch.controlplane.core.organization.dto.OrganizationDetails;
import dev.nexcraft.latch.controlplane.core.organization.dto.OrganizationName;
import dev.nexcraft.latch.controlplane.core.organization.dto.OrganizationPage;
import dev.nexcraft.latch.controlplane.core.organization.query.ListOrganizationsQuery;
import dev.nexcraft.latch.controlplane.core.organization.service.OrganizationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Handwritten HTTP controller for the organization API.
 *
 * <p>The OpenAPI document describes this controller's public contract but does
 * not generate its Java types.</p>
 */
@ApplicationScoped
@Path("/api/v1/organizations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationController {

    private final OrganizationService service;

    /**
     * Creates the organization controller.
     *
     * @param service organization application service
     */
    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    /**
     * Creates an organization.
     *
     * @param request organization name request
     * @return created organization response
     */
    @POST
    public Response createOrganization(@NotNull @Valid OrganizationName request) {
        Organization organization = service.create(new CreateOrganizationCommand(request.name()));
        return Response.status(Response.Status.CREATED).entity(toResponse(organization)).build();
    }

    /**
     * Lists active organizations.
     *
     * @param search optional name or slug search text
     * @param page zero-based page number
     * @param size requested page size
     * @param sort field and direction expression
     * @return paginated organization response
     */
    @GET
    public Response listOrganizations(
            @QueryParam("search") String search,
            @QueryParam("page") @DefaultValue("0") @Min(0) Integer page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) Integer size,
            @QueryParam("sort") @DefaultValue("createdAt,desc") String sort) {
        OrganizationPage response = toPage(service.list(
                new ListOrganizationsQuery(search, page, size, sort)));
        return Response.ok(response).build();
    }

    /**
     * Gets an active organization.
     *
     * @param organizationId organization identifier
     * @return organization response
     */
    @GET
    @Path("/{organizationId}")
    public Response getOrganization(@PathParam("organizationId") UUID organizationId) {
        return Response.ok(toResponse(service.get(organizationId))).build();
    }

    /**
     * Updates an organization's display name without changing its slug.
     *
     * @param organizationId organization identifier
     * @param request organization name request
     * @return updated organization response
     */
    @PATCH
    @Path("/{organizationId}")
    public Response updateOrganization(
            @PathParam("organizationId") UUID organizationId,
            @NotNull @Valid OrganizationName request) {
        Organization organization = service.update(
                new UpdateOrganizationCommand(organizationId, request.name()));
        return Response.ok(toResponse(organization)).build();
    }

    /**
     * Soft-deletes an organization.
     *
     * @param organizationId organization identifier
     * @return empty 204 response
     */
    @DELETE
    @Path("/{organizationId}")
    public Response deleteOrganization(@PathParam("organizationId") UUID organizationId) {
        service.delete(organizationId);
        return Response.noContent().build();
    }

    private OrganizationDetails toResponse(Organization organization) {
        return new OrganizationDetails(
                organization.id(),
                organization.name(),
                organization.slug(),
                organization.status(),
                organization.createdAt(),
                organization.updatedAt());
    }

    private OrganizationPage toPage(
            dev.nexcraft.latch.controlplane.core.organization.query.OrganizationPage page) {
        List<OrganizationDetails> items = page.items().stream()
                .map(this::toResponse)
                .toList();
        return new OrganizationPage(
                items,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}

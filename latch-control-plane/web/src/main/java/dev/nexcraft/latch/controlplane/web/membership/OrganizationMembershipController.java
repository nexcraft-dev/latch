package dev.nexcraft.latch.controlplane.web.membership;

import dev.nexcraft.latch.controlplane.core.membership.command.AddOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.RemoveOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.UpdateOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.dto.AddOrganizationMember;
import dev.nexcraft.latch.controlplane.core.membership.dto.MemberPage;
import dev.nexcraft.latch.controlplane.core.membership.dto.UpdateMembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.query.ListOrganizationMembersQuery;
import dev.nexcraft.latch.controlplane.core.membership.service.MembershipService;
import dev.nexcraft.latch.controlplane.web.security.CurrentIdentityContext;
import io.quarkus.security.Authenticated;
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
import java.util.UUID;

/**
 * Handwritten HTTP controller for Organization membership APIs.
 */
@ApplicationScoped
@Path("/api/v1/organizations/{organizationId}/members")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class OrganizationMembershipController {

    private final MembershipService service;
    private final CurrentIdentityContext currentIdentityContext;

    /**
     * Creates the membership controller.
     *
     * @param service membership application service
     * @param currentIdentityContext authenticated request Identity
     */
    public OrganizationMembershipController(
            MembershipService service,
            CurrentIdentityContext currentIdentityContext) {
        this.service = service;
        this.currentIdentityContext = currentIdentityContext;
    }

    /**
     * Lists active members of an accessible Organization.
     *
     * @param organizationId target Organization
     * @param page zero-based page number
     * @param size requested page size
     * @return member page
     */
    @GET
    public Response listMembers(
            @PathParam("organizationId") UUID organizationId,
            @QueryParam("page") @DefaultValue("0") @Min(0) Integer page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) Integer size) {
        MemberPage members = service.list(new ListOrganizationMembersQuery(
                currentIdentityContext.require().id(), organizationId, page, size));
        return Response.ok(members).build();
    }

    /**
     * Adds an Identity to an accessible Organization.
     *
     * @param organizationId target Organization
     * @param request member input
     * @return created member
     */
    @POST
    public Response addMember(
            @PathParam("organizationId") UUID organizationId,
            @NotNull @Valid AddOrganizationMember request) {
        var member = service.add(new AddOrganizationMemberCommand(
                currentIdentityContext.require().id(),
                organizationId,
                request.identityId(),
                request.role()));
        return Response.status(Response.Status.CREATED).entity(member).build();
    }

    /**
     * Changes a member role.
     *
     * @param organizationId target Organization
     * @param membershipId target membership
     * @param request role input
     * @return updated member
     */
    @PATCH
    @Path("/{membershipId}")
    public Response updateMember(
            @PathParam("organizationId") UUID organizationId,
            @PathParam("membershipId") UUID membershipId,
            @NotNull @Valid UpdateMembershipRole request) {
        var member = service.update(new UpdateOrganizationMemberCommand(
                currentIdentityContext.require().id(),
                organizationId,
                membershipId,
                request.role()));
        return Response.ok(member).build();
    }

    /**
     * Removes a membership row.
     *
     * @param organizationId target Organization
     * @param membershipId target membership
     * @return empty response
     */
    @DELETE
    @Path("/{membershipId}")
    public Response removeMember(
            @PathParam("organizationId") UUID organizationId,
            @PathParam("membershipId") UUID membershipId) {
        service.remove(new RemoveOrganizationMemberCommand(
                currentIdentityContext.require().id(), organizationId, membershipId));
        return Response.noContent().build();
    }
}

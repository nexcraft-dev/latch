package dev.nexcraft.latch.controlplane.web.project;

import dev.nexcraft.latch.controlplane.core.project.Project;
import dev.nexcraft.latch.controlplane.core.project.command.CreateProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.command.DeleteProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.command.UpdateProjectCommand;
import dev.nexcraft.latch.controlplane.core.project.dto.ProjectCreate;
import dev.nexcraft.latch.controlplane.core.project.dto.ProjectDetails;
import dev.nexcraft.latch.controlplane.core.project.dto.ProjectPage;
import dev.nexcraft.latch.controlplane.core.project.dto.ProjectUpdate;
import dev.nexcraft.latch.controlplane.core.project.query.GetProjectQuery;
import dev.nexcraft.latch.controlplane.core.project.query.ListProjectsQuery;
import dev.nexcraft.latch.controlplane.core.project.service.ProjectService;
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
import java.util.List;
import java.util.UUID;

/**
 * Handwritten HTTP controller for Organization-scoped Project APIs.
 *
 * <p>The OpenAPI document describes this controller's public contract but does
 * not generate its Java types.</p>
 */
@ApplicationScoped
@Path("/api/v1/organizations/{organizationId}/projects")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ProjectController {

    private final ProjectService service;
    private final CurrentIdentityContext currentIdentityContext;

    /**
     * Creates the Project controller.
     *
     * @param service Project application service
     * @param currentIdentityContext authenticated request Identity
     */
    public ProjectController(ProjectService service, CurrentIdentityContext currentIdentityContext) {
        this.service = service;
        this.currentIdentityContext = currentIdentityContext;
    }

    /**
     * Creates a Project in an active Organization.
     *
     * @param organizationId target Organization
     * @param request Project creation input
     * @return created Project response
     */
    @POST
    public Response createProject(
            @PathParam("organizationId") UUID organizationId,
            @NotNull @Valid ProjectCreate request) {
        Project project = service.create(new CreateProjectCommand(
                currentIdentityContext.require().id(),
                organizationId,
                request.name(),
                request.key(),
                request.description()));
        return Response.status(Response.Status.CREATED).entity(toResponse(project)).build();
    }

    /**
     * Lists active Projects in an accessible Organization.
     *
     * @param organizationId target Organization
     * @param search optional search text
     * @param page zero-based page number
     * @param size requested page size
     * @param sort field and direction expression
     * @return paginated Project response
     */
    @GET
    public Response listProjects(
            @PathParam("organizationId") UUID organizationId,
            @QueryParam("search") String search,
            @QueryParam("page") @DefaultValue("0") @Min(0) Integer page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) Integer size,
            @QueryParam("sort") @DefaultValue("createdAt,desc") String sort) {
        dev.nexcraft.latch.controlplane.core.project.query.ProjectPage projects = service.list(
                new ListProjectsQuery(
                        currentIdentityContext.require().id(), organizationId, search, page, size, sort));
        return Response.ok(toPage(projects)).build();
    }

    /**
     * Gets an active Project in an accessible Organization.
     *
     * @param organizationId target Organization
     * @param projectId target Project
     * @return Project response
     */
    @GET
    @Path("/{projectId}")
    public Response getProject(
            @PathParam("organizationId") UUID organizationId,
            @PathParam("projectId") UUID projectId) {
        Project project = service.get(new GetProjectQuery(
                currentIdentityContext.require().id(), organizationId, projectId));
        return Response.ok(toResponse(project)).build();
    }

    /**
     * Updates a Project name and description without changing its key.
     *
     * @param organizationId target Organization
     * @param projectId target Project
     * @param request mutable Project fields
     * @return updated Project response
     */
    @PATCH
    @Path("/{projectId}")
    public Response updateProject(
            @PathParam("organizationId") UUID organizationId,
            @PathParam("projectId") UUID projectId,
            @NotNull @Valid ProjectUpdate request) {
        Project project = service.update(new UpdateProjectCommand(
                currentIdentityContext.require().id(),
                organizationId,
                projectId,
                request.name(),
                request.description()));
        return Response.ok(toResponse(project)).build();
    }

    /**
     * Soft-deletes a Project.
     *
     * @param organizationId target Organization
     * @param projectId target Project
     * @return empty 204 response
     */
    @DELETE
    @Path("/{projectId}")
    public Response deleteProject(
            @PathParam("organizationId") UUID organizationId,
            @PathParam("projectId") UUID projectId) {
        service.delete(new DeleteProjectCommand(
                currentIdentityContext.require().id(), organizationId, projectId));
        return Response.noContent().build();
    }

    private ProjectDetails toResponse(Project project) {
        return new ProjectDetails(
                project.id(),
                project.organizationId(),
                project.name(),
                project.key(),
                project.description(),
                project.status(),
                project.createdAt(),
                project.updatedAt());
    }

    private ProjectPage toPage(
            dev.nexcraft.latch.controlplane.core.project.query.ProjectPage page) {
        List<ProjectDetails> items = page.items().stream()
                .map(this::toResponse)
                .toList();
        return new ProjectPage(
                items,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}

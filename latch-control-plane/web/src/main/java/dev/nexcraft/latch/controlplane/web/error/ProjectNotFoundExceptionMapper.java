package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.project.error.ProjectNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps missing or deleted Projects to HTTP 404 problems.
 */
@Provider
public class ProjectNotFoundExceptionMapper implements ExceptionMapper<ProjectNotFoundException> {

    @Override
    public Response toResponse(ProjectNotFoundException exception) {
        return HttpProblemResponse.build(404, "Not Found", exception.getMessage());
    }
}

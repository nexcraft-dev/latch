package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.project.error.ProjectValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps Project validation failures to HTTP 400 problems.
 */
@Provider
public class ProjectValidationExceptionMapper implements ExceptionMapper<ProjectValidationException> {

    @Override
    public Response toResponse(ProjectValidationException exception) {
        return HttpProblemResponse.build(400, "Bad Request", exception.getMessage());
    }
}

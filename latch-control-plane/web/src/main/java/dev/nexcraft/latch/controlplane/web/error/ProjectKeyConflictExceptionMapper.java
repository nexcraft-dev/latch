package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.project.error.ProjectKeyConflictException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps duplicate Organization-local Project keys to HTTP 409 problems.
 */
@Provider
public class ProjectKeyConflictExceptionMapper implements ExceptionMapper<ProjectKeyConflictException> {

    @Override
    public Response toResponse(ProjectKeyConflictException exception) {
        return HttpProblemResponse.build(409, "Conflict", exception.getMessage());
    }
}

package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps missing or deleted organizations to HTTP 404 problems.
 */
@Provider
public class OrganizationNotFoundExceptionMapper implements ExceptionMapper<OrganizationNotFoundException> {

    @Override
    public Response toResponse(OrganizationNotFoundException exception) {
        return HttpProblemResponse.build(404, "Not Found", exception.getMessage());
    }
}

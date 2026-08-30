package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps domain validation failures to HTTP 400 problems.
 */
@Provider
public class OrganizationValidationExceptionMapper
        implements ExceptionMapper<OrganizationValidationException> {

    @Override
    public Response toResponse(OrganizationValidationException exception) {
        return HttpProblemResponse.build(400, "Bad Request", exception.getMessage());
    }
}

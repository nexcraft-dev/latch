package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationSlugConflictException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps exhausted slug generation attempts to HTTP 409 problems.
 */
@Provider
public class OrganizationSlugConflictExceptionMapper
        implements ExceptionMapper<OrganizationSlugConflictException> {

    @Override
    public Response toResponse(OrganizationSlugConflictException exception) {
        return HttpProblemResponse.build(409, "Conflict", exception.getMessage());
    }
}

package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.membership.error.MembershipForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps insufficient membership roles to HTTP 403 problems.
 */
@Provider
public class MembershipForbiddenExceptionMapper implements ExceptionMapper<MembershipForbiddenException> {

    @Override
    public Response toResponse(MembershipForbiddenException exception) {
        return HttpProblemResponse.build(403, "Forbidden", exception.getMessage());
    }
}

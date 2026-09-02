package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.membership.error.MembershipNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps missing memberships to HTTP 404 problems.
 */
@Provider
public class MembershipNotFoundExceptionMapper implements ExceptionMapper<MembershipNotFoundException> {

    @Override
    public Response toResponse(MembershipNotFoundException exception) {
        return HttpProblemResponse.build(404, "Not Found", exception.getMessage());
    }
}

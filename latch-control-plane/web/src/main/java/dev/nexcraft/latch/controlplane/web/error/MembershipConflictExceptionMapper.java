package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.membership.error.MembershipConflictException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps duplicate membership operations to HTTP 409 problems.
 */
@Provider
public class MembershipConflictExceptionMapper implements ExceptionMapper<MembershipConflictException> {

    @Override
    public Response toResponse(MembershipConflictException exception) {
        return HttpProblemResponse.build(409, "Conflict", exception.getMessage());
    }
}

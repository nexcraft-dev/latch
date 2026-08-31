package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.membership.error.MembershipValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps membership validation failures to HTTP 400 problems.
 */
@Provider
public class MembershipValidationExceptionMapper implements ExceptionMapper<MembershipValidationException> {

    @Override
    public Response toResponse(MembershipValidationException exception) {
        return HttpProblemResponse.build(400, "Bad Request", exception.getMessage());
    }
}

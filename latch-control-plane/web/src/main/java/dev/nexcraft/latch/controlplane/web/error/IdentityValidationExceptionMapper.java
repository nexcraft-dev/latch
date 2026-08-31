package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.identity.error.IdentityValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps invalid authenticated Identity claims to HTTP 401 problems.
 */
@Provider
public class IdentityValidationExceptionMapper implements ExceptionMapper<IdentityValidationException> {

    @Override
    public Response toResponse(IdentityValidationException exception) {
        return HttpProblemResponse.build(401, "Unauthorized", exception.getMessage());
    }
}

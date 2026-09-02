package dev.nexcraft.latch.controlplane.web.error;

import dev.nexcraft.latch.controlplane.core.identity.error.IdentityNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps missing Identities to HTTP 404 problems.
 */
@Provider
public class IdentityNotFoundExceptionMapper implements ExceptionMapper<IdentityNotFoundException> {

    @Override
    public Response toResponse(IdentityNotFoundException exception) {
        return HttpProblemResponse.build(404, "Not Found", exception.getMessage());
    }
}

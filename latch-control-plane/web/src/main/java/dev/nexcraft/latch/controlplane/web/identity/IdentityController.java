package dev.nexcraft.latch.controlplane.web.identity;

import dev.nexcraft.latch.controlplane.core.identity.dto.IdentityDetails;
import dev.nexcraft.latch.controlplane.web.security.CurrentIdentityContext;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Handwritten HTTP controller for the current authenticated Identity.
 */
@ApplicationScoped
@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class IdentityController {

    private final CurrentIdentityContext currentIdentityContext;

    /**
     * Creates the current Identity controller.
     *
     * @param currentIdentityContext authenticated request Identity
     */
    public IdentityController(CurrentIdentityContext currentIdentityContext) {
        this.currentIdentityContext = currentIdentityContext;
    }

    /**
     * Returns the safe current Identity representation.
     *
     * @return current Identity response
     */
    @GET
    public Response getCurrentIdentity() {
        var identity = currentIdentityContext.require();
        return Response.ok(new IdentityDetails(identity.id(), identity.email(), identity.displayName())).build();
    }
}

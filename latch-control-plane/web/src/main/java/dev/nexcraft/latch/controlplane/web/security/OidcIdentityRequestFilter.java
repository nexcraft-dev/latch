package dev.nexcraft.latch.controlplane.web.security;

import dev.nexcraft.latch.controlplane.core.identity.IdentityClaims;
import dev.nexcraft.latch.controlplane.core.identity.service.IdentityService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.security.Principal;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Resolves the durable core Identity from Quarkus' verified OIDC principal.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class OidcIdentityRequestFilter implements ContainerRequestFilter {

    private final SecurityIdentity securityIdentity;
    private final IdentityService identityService;
    private final CurrentIdentityContext currentIdentityContext;

    /**
     * Creates the OIDC request filter.
     *
     * @param securityIdentity Quarkus request security identity
     * @param identityService application Identity service
     * @param currentIdentityContext request-scoped Identity context
     */
    @Inject
    public OidcIdentityRequestFilter(
            SecurityIdentity securityIdentity,
            IdentityService identityService,
            CurrentIdentityContext currentIdentityContext) {
        this.securityIdentity = securityIdentity;
        this.identityService = identityService;
        this.currentIdentityContext = currentIdentityContext;
    }

    /**
     * Resolves Identity data only for an authenticated product request.
     *
     * @param requestContext JAX-RS request context
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (isHealthRequest(requestContext) || securityIdentity.isAnonymous()) {
            return;
        }
        JsonWebToken token = verifiedToken(securityIdentity.getPrincipal());
        String provider = required(token.getIssuer());
        String providerSubject = required(token.getSubject());
        String email = claimAsString(token, "email");
        String displayName = claimAsString(token, "name");
        if (displayName == null) {
            displayName = claimAsString(token, "preferred_username");
        }
        currentIdentityContext.set(identityService.resolveOrCreate(
                new IdentityClaims(provider, providerSubject, email, displayName)));
    }

    private JsonWebToken verifiedToken(Principal principal) {
        if (!(principal instanceof JsonWebToken token)) {
            throw new jakarta.ws.rs.NotAuthorizedException("Bearer");
        }
        return token;
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new jakarta.ws.rs.NotAuthorizedException("Bearer");
        }
        return value;
    }

    private String claimAsString(JsonWebToken token, String claimName) {
        Object value = token.getClaim(claimName);
        if (value == null) {
            return null;
        }
        String stringValue = value instanceof String ? (String) value : null;
        return stringValue == null || stringValue.isBlank() ? null : stringValue;
    }

    private boolean isHealthRequest(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        return normalizedPath.equals("q/health") || normalizedPath.startsWith("q/health/");
    }
}

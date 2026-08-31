package dev.nexcraft.latch.controlplane.web.organization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

/**
 * Verifies the packaged Organization application exposes a secured product surface.
 */
@QuarkusIntegrationTest
class OrganizationControllerIT {

    @TestHTTPResource("/")
    URL baseUrl;

    /**
     * Confirms packaged product requests reject missing bearer credentials.
     *
     * <p>Authenticated packaged testing requires a live external OIDC provider;
     * deterministic authenticated coverage is provided by the JVM suite.</p>
     */
    @Test
    void packagedOrganizationEndpointRequiresAuthentication() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(resolve("/api/v1/organizations"))
                .GET()
                .build();

        HttpResponse<Void> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.discarding());

        assertTrue(response.statusCode() == 401 || response.statusCode() == 403);
    }

    private URI resolve(String path) {
        return URI.create(baseUrl.toString()).resolve(path.substring(1));
    }
}

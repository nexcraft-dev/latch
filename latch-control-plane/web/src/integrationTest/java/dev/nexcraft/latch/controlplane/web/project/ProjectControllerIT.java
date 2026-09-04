package dev.nexcraft.latch.controlplane.web.project;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies the packaged Project endpoint requires authentication.
 */
@QuarkusIntegrationTest
class ProjectControllerIT {

    @TestHTTPResource("/")
    URL baseUrl;

    /**
     * Confirms packaged Project requests reject missing bearer credentials.
     *
     * <p>Authenticated packaged testing requires a live external OIDC
     * provider; deterministic authenticated coverage is provided by the JVM
     * suite.</p>
     */
    @Test
    void packagedProjectEndpointRequiresAuthentication() throws Exception {
        String path = "/api/v1/organizations/" + UUID.randomUUID() + "/projects";
        HttpRequest request = HttpRequest.newBuilder(resolve(path)).GET().build();

        HttpResponse<Void> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.discarding());

        assertTrue(response.statusCode() == 401 || response.statusCode() == 403);
    }

    private URI resolve(String path) {
        return URI.create(baseUrl.toString()).resolve(path.substring(1));
    }
}

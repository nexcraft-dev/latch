package dev.nexcraft.latch.controlplane.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import org.junit.jupiter.api.Test;

/**
 * Verifies the packaged application through the Quarkus integration-test task.
 */
@QuarkusIntegrationTest
class HealthCheckIT {

    @TestHTTPResource("/q/health")
    URL healthUrl;

    /**
     * Confirms that the packaged application reports a healthy runtime.
     *
     * @throws IOException when the local test request cannot be sent
     * @throws InterruptedException when the test thread is interrupted
     */
    @Test
    void packagedHealthEndpointReportsUp() throws IOException, InterruptedException, URISyntaxException {
        HttpResponse<String> response = sendHealthRequest();

        assertEquals(200, response.statusCode());
        assertTrue(response.body().matches("(?s).*\\\"status\\\"\\s*:\\s*\\\"UP\\\".*"), response.body());
    }

    private HttpResponse<String> sendHealthRequest()
            throws IOException, InterruptedException, URISyntaxException {
        URI uri = healthUrl.toURI();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}

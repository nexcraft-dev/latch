package dev.nexcraft.latch.controlplane.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

/**
 * Verifies product endpoints require bearer authentication while health remains available.
 */
@QuarkusTest
class AuthenticationTest {

    @TestHTTPResource("/")
    URL baseUrl;

    /**
     * Confirms unauthenticated product requests are rejected.
     */
    @Test
    void rejectsUnauthenticatedProductEndpoints() throws Exception {
        assertEquals(401, request("/api/v1/me"));
        assertEquals(401, request("/api/v1/organizations"));
    }

    private int request(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl.toString()).resolve(path.substring(1)))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}

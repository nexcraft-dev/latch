package dev.nexcraft.latch.controlplane.web.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

/**
 * Verifies the current authenticated Identity endpoint.
 */
@QuarkusTest
@TestSecurity(user = "identity-user")
@OidcSecurity(claims = {
        @Claim(key = "iss", value = "https://issuer.example"),
        @Claim(key = "sub", value = "identity-subject"),
        @Claim(key = "email", value = "identity@example.com"),
        @Claim(key = "name", value = "Identity User")
})
class IdentityControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestHTTPResource("/")
    URL baseUrl;

    /**
     * Confirms verified OIDC claims resolve to a safe current Identity response.
     */
    @Test
    void returnsCurrentIdentityWithoutExternalIdentityKey() throws Exception {
        HttpResponse<String> response = request("GET", "/api/v1/me", null);
        JsonNode body = objectMapper.readTree(response.body());

        assertEquals(200, response.statusCode());
        assertTrue(body.get("id").isTextual());
        assertEquals("identity@example.com", body.get("email").asText());
        assertEquals("Identity User", body.get("displayName").asText());
        assertFalse(body.has("provider"));
        assertFalse(body.has("providerSubject"));
    }

    private HttpResponse<String> request(String method, String path, String body)
            throws Exception {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(resolve(path))
                .method(method, publisher)
                .header("Accept", "application/json, application/problem+json")
                .header("Content-Type", "application/json")
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI resolve(String path) {
        return URI.create(baseUrl.toString()).resolve(path.substring(1));
    }
}

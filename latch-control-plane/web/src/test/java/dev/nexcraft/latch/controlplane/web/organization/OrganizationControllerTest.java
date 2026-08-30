package dev.nexcraft.latch.controlplane.web.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Verifies the Organization REST contract against the Quarkus JVM runtime.
 */
@QuarkusTest
class OrganizationControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestHTTPResource("/")
    URL baseUrl;

    /**
     * Verifies creation, generated slugging, and active retrieval.
     *
     * @throws Exception when the local HTTP request or JSON parsing fails
     */
    @Test
    void createsAndGetsOrganization() throws Exception {
        HttpResponse<String> created = request("POST", "/api/v1/organizations", "{\"name\":\"Acme Corporation\"}");
        JsonNode organization = read(created);

        assertEquals(201, created.statusCode());
        assertEquals("acme-corporation", organization.get("slug").asText());
        assertEquals("ACTIVE", organization.get("status").asText());
        assertEquals(200, request("GET", "/api/v1/organizations/" + organization.get("id").asText(), null).statusCode());
    }

    /**
     * Verifies that updates preserve the slug and deletes hide the resource.
     *
     * @throws Exception when the local HTTP request or JSON parsing fails
     */
    @Test
    void updatesWithoutChangingSlugAndSoftDeletes() throws Exception {
        JsonNode created = read(request("POST", "/api/v1/organizations", "{\"name\":\"Update Source\"}"));
        String id = created.get("id").asText();
        String slug = created.get("slug").asText();

        HttpResponse<String> updatedResponse = request(
                "PATCH", "/api/v1/organizations/" + id, "{\"name\":\"Updated Destination\"}");
        JsonNode updated = read(updatedResponse);
        HttpResponse<String> deleted = request("DELETE", "/api/v1/organizations/" + id, null);

        assertEquals(200, updatedResponse.statusCode());
        assertEquals("Updated Destination", updated.get("name").asText());
        assertEquals(slug, updated.get("slug").asText());
        assertEquals(204, deleted.statusCode());
        assertEquals(404, request("GET", "/api/v1/organizations/" + id, null).statusCode());
    }

    /**
     * Verifies case-insensitive search, sorting, and pagination metadata.
     *
     * @throws Exception when the local HTTP request or JSON parsing fails
     */
    @Test
    void listsActiveOrganizationsWithSearchSortAndPagination() throws Exception {
        String marker = "List Marker " + System.nanoTime();
        request("POST", "/api/v1/organizations", "{\"name\":\"" + marker + " Zebra\"}");
        request("POST", "/api/v1/organizations", "{\"name\":\"" + marker + " Alpha\"}");

        String query = URLEncoder.encode(marker.toLowerCase(), StandardCharsets.UTF_8);
        HttpResponse<String> response = request(
                "GET",
                "/api/v1/organizations?search=" + query + "&page=0&size=1&sort=name,asc",
                null);
        JsonNode page = read(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, page.get("items").size());
        assertTrue(page.get("items").get(0).get("name").asText().endsWith(" Alpha"));
        assertEquals(0, page.get("page").asInt());
        assertEquals(2, page.get("totalElements").asInt());
        assertEquals(2, page.get("totalPages").asInt());
    }

    /**
     * Verifies request and query validation use RFC 7807 responses.
     *
     * @throws Exception when the local HTTP request or JSON parsing fails
     */
    @Test
    void rejectsInvalidRequestsAsHttpProblems() throws Exception {
        HttpResponse<String> blankName = request("POST", "/api/v1/organizations", "{\"name\":\" \"}");
        HttpResponse<String> missingName = request("POST", "/api/v1/organizations", "{}");
        HttpResponse<String> oversizedName = request(
                "POST", "/api/v1/organizations", "{\"name\":\"" + "a".repeat(121) + "\"}");
        HttpResponse<String> malformedJson = request("POST", "/api/v1/organizations", "{\"name\":");
        HttpResponse<String> invalidSort = request("GET", "/api/v1/organizations?sort=id,asc", null);
        HttpResponse<String> invalidSize = request("GET", "/api/v1/organizations?size=101", null);

        JsonNode problem = read(blankName);
        JsonNode malformedJsonProblem = read(malformedJson);
        assertEquals(400, blankName.statusCode());
        assertEquals(400, problem.get("status").asInt());
        assertEquals(400, missingName.statusCode());
        assertEquals(400, oversizedName.statusCode());
        assertFalse(blankName.headers().firstValue("content-type").orElse("").isEmpty());
        assertEquals(400, malformedJson.statusCode());
        assertEquals(400, malformedJsonProblem.get("status").asInt());
        assertTrue(malformedJson.headers().firstValue("content-type").orElse("")
                .contains("application/problem+json"));
        assertEquals(400, invalidSort.statusCode());
        assertEquals(400, invalidSize.statusCode());
    }

    private JsonNode read(HttpResponse<String> response) throws IOException {
        return objectMapper.readTree(response.body());
    }

    private HttpResponse<String> request(String method, String path, String body)
            throws IOException, InterruptedException {
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

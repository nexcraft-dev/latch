package dev.nexcraft.latch.controlplane.web.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
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
 * Verifies the Organization-scoped Project REST contract against Quarkus JVM.
 */
@QuarkusTest
@TestSecurity(user = "project-owner")
@OidcSecurity(claims = {
        @Claim(key = "iss", value = "https://issuer.example"),
        @Claim(key = "sub", value = "project-owner-subject"),
        @Claim(key = "email", value = "project-owner@example.com"),
        @Claim(key = "name", value = "Project Owner")
})
class ProjectControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestHTTPResource("/")
    URL baseUrl;

    /**
     * Verifies the complete Project lifecycle and immutable key behavior.
     */
    @Test
    void createsListsUpdatesAndDeletesProject() throws Exception {
        String marker = "Project Lifecycle " + System.nanoTime();
        JsonNode organization = read(request(
                "POST", "/api/v1/organizations", "{\"name\":\"" + marker + " Organization\"}"));
        String organizationId = organization.get("id").asText();
        String projectsPath = "/api/v1/organizations/" + organizationId + "/projects";

        HttpResponse<String> createdResponse = request(
                "POST",
                projectsPath,
                "{\"name\":\"Checkout\",\"key\":\"checkout-" + keySuffix(marker)
                        + "\",\"description\":\"" + marker + " description\"}");
        JsonNode created = read(createdResponse);
        String projectId = created.get("id").asText();
        String key = created.get("key").asText();

        HttpResponse<String> listedResponse = request(
                "GET",
                projectsPath + "?search=" + encode(marker.toLowerCase()) + "&sort=name,asc",
                null);
        JsonNode listed = read(listedResponse);
        HttpResponse<String> updatedResponse = request(
                "PATCH",
                projectsPath + "/" + projectId,
                "{\"name\":\"Checkout Platform\",\"description\":\"Updated\"}");
        JsonNode updated = read(updatedResponse);
        HttpResponse<String> deletedResponse = request("DELETE", projectsPath + "/" + projectId, null);

        assertEquals(201, createdResponse.statusCode());
        assertEquals(organizationId, created.get("organizationId").asText());
        assertEquals("ACTIVE", created.get("status").asText());
        assertEquals(1, listed.get("totalElements").asInt());
        assertEquals(200, listedResponse.statusCode());
        assertEquals(200, updatedResponse.statusCode());
        assertEquals("Checkout Platform", updated.get("name").asText());
        assertEquals(key, updated.get("key").asText());
        assertEquals(204, deletedResponse.statusCode());
        assertEquals(404, request("GET", projectsPath + "/" + projectId, null).statusCode());
    }

    /**
     * Verifies case-insensitive search across Project description and metadata.
     */
    @Test
    void searchesAndSortsActiveProjects() throws Exception {
        String marker = "Project Search " + System.nanoTime();
        JsonNode organization = read(request(
                "POST", "/api/v1/organizations", "{\"name\":\"" + marker + " Organization\"}"));
        String projectsPath = "/api/v1/organizations/" + organization.get("id").asText() + "/projects";
        request(
                "POST",
                projectsPath,
                "{\"name\":\"Zebra\",\"key\":\"zebra-" + keySuffix(marker)
                        + "\",\"description\":\"Contains UniqueDescription\"}");
        request(
                "POST",
                projectsPath,
                "{\"name\":\"Alpha\",\"key\":\"alpha-" + keySuffix(marker)
                        + "\",\"description\":\"Another project\"}");

        HttpResponse<String> response = request(
                "GET",
                projectsPath + "?search=uniquedescription&page=0&size=20&sort=name,asc",
                null);
        JsonNode page = read(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, page.get("totalElements").asInt());
        assertEquals("Zebra", page.get("items").get(0).get("name").asText());
        assertEquals(1, page.get("totalPages").asInt());
    }

    /**
     * Verifies Project input and query validation use RFC 7807 responses.
     */
    @Test
    void rejectsInvalidProjectRequests() throws Exception {
        JsonNode organization = read(request(
                "POST", "/api/v1/organizations", "{\"name\":\"Project Validation "
                        + System.nanoTime() + "\"}"));
        String projectsPath = "/api/v1/organizations/" + organization.get("id").asText() + "/projects";
        HttpResponse<String> blankName = request(
                "POST", projectsPath, "{\"name\":\" \",\"key\":\"valid-key\"}");
        HttpResponse<String> invalidKey = request(
                "POST", projectsPath, "{\"name\":\"Valid\",\"key\":\"Invalid_Key\"}");
        HttpResponse<String> oversizedDescription = request(
                "POST",
                projectsPath,
                "{\"name\":\"Valid\",\"key\":\"valid-description\",\"description\":\""
                        + "a".repeat(501) + "\"}");
        HttpResponse<String> invalidSort = request("GET", projectsPath + "?sort=description,asc", null);
        HttpResponse<String> invalidPage = request("GET", projectsPath + "?page=-1", null);
        HttpResponse<String> invalidSize = request("GET", projectsPath + "?size=101", null);

        assertEquals(400, blankName.statusCode());
        assertEquals(400, invalidKey.statusCode());
        assertEquals(400, oversizedDescription.statusCode());
        assertEquals(400, invalidSort.statusCode());
        assertEquals(400, invalidPage.statusCode());
        assertEquals(400, invalidSize.statusCode());
        assertTrue(blankName.headers().firstValue("content-type").orElse("")
                .contains("application/problem+json"));
        assertEquals(400, read(blankName).get("status").asInt());
    }

    /**
     * Verifies duplicate keys and Organization path mismatches are rejected.
     */
    @Test
    void rejectsDuplicateKeysAndCrossOrganizationAccess() throws Exception {
        JsonNode firstOrganization = read(request(
                "POST", "/api/v1/organizations", "{\"name\":\"Project First "
                        + System.nanoTime() + "\"}"));
        JsonNode secondOrganization = read(request(
                "POST", "/api/v1/organizations", "{\"name\":\"Project Second "
                        + System.nanoTime() + "\"}"));
        String firstPath = "/api/v1/organizations/" + firstOrganization.get("id").asText() + "/projects";
        String secondPath = "/api/v1/organizations/" + secondOrganization.get("id").asText() + "/projects";
        JsonNode created = read(request(
                "POST", firstPath, "{\"name\":\"Shared\",\"key\":\"shared-key\"}"));
        HttpResponse<String> duplicate = request(
                "POST", firstPath, "{\"name\":\"Duplicate\",\"key\":\"shared-key\"}");
        String projectId = created.get("id").asText();

        HttpResponse<String> mismatchedGet = request("GET", secondPath + "/" + projectId, null);
        HttpResponse<String> mismatchedUpdate = request(
                "PATCH", secondPath + "/" + projectId, "{\"name\":\"Changed\"}");

        assertEquals(409, duplicate.statusCode());
        assertEquals(404, mismatchedGet.statusCode());
        assertEquals(404, mismatchedUpdate.statusCode());
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

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String keySuffix(String value) {
        return Integer.toUnsignedString(value.hashCode());
    }
}

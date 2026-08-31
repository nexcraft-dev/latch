package dev.nexcraft.latch.controlplane.web.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.core.identity.IdentityClaims;
import dev.nexcraft.latch.controlplane.repository.identity.IdentityRepository;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Verifies Organization membership REST behavior against the Quarkus JVM runtime.
 */
@QuarkusTest
@TestSecurity(user = "membership-owner")
@OidcSecurity(claims = {
        @Claim(key = "iss", value = "https://issuer.example"),
        @Claim(key = "sub", value = "membership-owner-subject"),
        @Claim(key = "email", value = "owner@example.com"),
        @Claim(key = "name", value = "Membership Owner")
})
class MembershipControllerTest {

    private static final Instant NOW = Instant.parse("2026-08-30T10:00:00Z");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    IdentityRepository identityRepository;

    @TestHTTPResource("/")
    URL baseUrl;

    /**
     * Confirms owner creation, member add, role update, removal, and Identity retention.
     */
    @Test
    void managesMembershipLifecycleWithoutDeletingIdentity() throws Exception {
        UUID targetId = seedIdentity("member-" + UUID.randomUUID());
        JsonNode organization = read(request(
                "POST", "/api/v1/organizations", "{\"name\":\"Membership Lifecycle\"}"));
        String organizationId = organization.get("id").asText();

        JsonNode initialPage = read(request(
                "GET", "/api/v1/organizations/" + organizationId + "/members", null));
        String ownerMembershipId = initialPage.get("items").get(0).get("id").asText();
        assertEquals("OWNER", initialPage.get("items").get(0).get("role").asText());

        HttpResponse<String> addedResponse = request(
                "POST",
                "/api/v1/organizations/" + organizationId + "/members",
                "{\"identityId\":\"" + targetId + "\",\"role\":\"MEMBER\"}");
        JsonNode added = read(addedResponse);
        String membershipId = added.get("id").asText();

        HttpResponse<String> updatedResponse = request(
                "PATCH",
                "/api/v1/organizations/" + organizationId + "/members/" + membershipId,
                "{\"role\":\"ADMIN\"}");
        JsonNode updated = read(updatedResponse);
        HttpResponse<String> removedResponse = request(
                "DELETE",
                "/api/v1/organizations/" + organizationId + "/members/" + membershipId,
                null);
        JsonNode finalPage = read(request(
                "GET", "/api/v1/organizations/" + organizationId + "/members", null));

        assertEquals(201, addedResponse.statusCode());
        assertEquals(targetId.toString(), added.get("identityId").asText());
        assertEquals("MEMBER", added.get("role").asText());
        assertEquals(200, updatedResponse.statusCode());
        assertEquals("ADMIN", updated.get("role").asText());
        assertEquals(204, removedResponse.statusCode());
        assertEquals(1, finalPage.get("totalElements").asInt());
        assertEquals(ownerMembershipId, finalPage.get("items").get(0).get("id").asText());
        assertTrue(identityRepository.findById(targetId).isPresent());
    }

    /**
     * Confirms duplicate membership is rejected and page validation is enforced.
     */
    @Test
    void rejectsDuplicateMembershipAndInvalidPageSize() throws Exception {
        UUID targetId = seedIdentity("duplicate-" + UUID.randomUUID());
        JsonNode organization = read(request(
                "POST", "/api/v1/organizations", "{\"name\":\"Membership Duplicate\"}"));
        String organizationId = organization.get("id").asText();
        String path = "/api/v1/organizations/" + organizationId + "/members";
        request("POST", path, "{\"identityId\":\"" + targetId + "\",\"role\":\"VIEWER\"}");

        HttpResponse<String> duplicate = request(
                "POST", path, "{\"identityId\":\"" + targetId + "\",\"role\":\"VIEWER\"}");
        HttpResponse<String> invalidPageSize = request("GET", path + "?size=101", null);

        assertEquals(409, duplicate.statusCode());
        assertEquals(409, objectMapper.readTree(duplicate.body()).get("status").asInt());
        assertEquals(400, invalidPageSize.statusCode());
    }

    private UUID seedIdentity(String subject) {
        UUID id = UUID.randomUUID();
        identityRepository.save(Identity.create(
                id,
                new IdentityClaims("https://issuer.example", subject, subject + "@example.com", subject),
                NOW));
        return id;
    }

    private JsonNode read(HttpResponse<String> response) throws Exception {
        return objectMapper.readTree(response.body());
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

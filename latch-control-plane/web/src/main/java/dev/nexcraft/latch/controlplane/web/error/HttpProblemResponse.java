package dev.nexcraft.latch.controlplane.web.error;

import io.quarkiverse.httpproblem.HttpProblem;
import jakarta.ws.rs.core.Response;
import java.net.URI;

/**
 * Creates HTTP problem responses through the Quarkiverse HTTP Problem extension.
 */
public final class HttpProblemResponse {

    private HttpProblemResponse() {
    }

    /**
     * Builds a safe RFC 9457/RFC 7807 response for a domain exception.
     *
     * @param status HTTP status
     * @param title short problem title
     * @param detail safe problem detail
     * @return HTTP problem response
     */
    public static Response build(int status, String title, String detail) {
        return HttpProblem.builder()
                .withType(URI.create("https://latch.dev/problems/" + status))
                .withTitle(title)
                .withStatus(status)
                .withDetail(detail)
                .withInstance(URI.create("/api/v1/organizations"))
                .build()
                .toResponse();
    }
}

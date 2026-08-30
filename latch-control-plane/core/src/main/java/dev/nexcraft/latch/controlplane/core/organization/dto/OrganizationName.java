package dev.nexcraft.latch.controlplane.core.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Organization name model used when creating or renaming an organization.
 *
 * @param name requested organization display name
 */
public record OrganizationName(
        @NotBlank(message = "Organization name must not be blank")
        @Size(max = 120, message = "Organization name must be at most 120 characters")
        String name) {
}

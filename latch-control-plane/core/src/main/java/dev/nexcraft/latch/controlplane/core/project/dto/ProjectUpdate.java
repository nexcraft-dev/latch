package dev.nexcraft.latch.controlplane.core.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * HTTP input model for changing mutable Project fields.
 *
 * @param name replacement display name
 * @param description optional replacement description; null retains the existing value
 */
public record ProjectUpdate(
        @NotBlank(message = "Project name must not be blank")
        @Size(max = 120, message = "Project name must be at most 120 characters")
        String name,
        @Size(max = 500, message = "Project description must be at most 500 characters")
        String description) {
}

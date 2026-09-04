package dev.nexcraft.latch.controlplane.core.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * HTTP input model for creating a Project.
 *
 * @param name display name
 * @param key stable Organization-local configuration key
 * @param description optional description
 */
public record ProjectCreate(
        @NotBlank(message = "Project name must not be blank")
        @Size(max = 120, message = "Project name must be at most 120 characters")
        String name,
        @NotBlank(message = "Project key must not be blank")
        @Size(max = 80, message = "Project key must be at most 80 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Project key must contain only lowercase letters, numbers, and hyphens")
        String key,
        @Size(max = 500, message = "Project description must be at most 500 characters")
        String description) {
}

package dev.nexcraft.latch.controlplane.core.organization.command;

import java.util.UUID;

/**
 * Input model for renaming an organization.
 *
 * @param id organization identifier
 * @param name replacement display name
 */
public record UpdateOrganizationCommand(UUID id, String name) {
}

package dev.nexcraft.latch.controlplane.core.organization.command;

/**
 * Input model for creating an organization.
 *
 * @param name requested display name
 */
public record CreateOrganizationCommand(String name) {
}

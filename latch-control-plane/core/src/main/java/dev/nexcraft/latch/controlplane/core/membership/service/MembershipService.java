package dev.nexcraft.latch.controlplane.core.membership.service;

import dev.nexcraft.latch.controlplane.core.membership.command.AddOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.RemoveOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.UpdateOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.dto.MemberPage;
import dev.nexcraft.latch.controlplane.core.membership.dto.OrganizationMember;
import dev.nexcraft.latch.controlplane.core.membership.query.ListOrganizationMembersQuery;

/**
 * Application contract for Organization membership use cases.
 */
public interface MembershipService {

    /**
     * Lists active members visible to the authenticated caller.
     *
     * @param query member listing input
     * @return active member page
     */
    MemberPage list(ListOrganizationMembersQuery query);

    /**
     * Adds a member to an Organization.
     *
     * @param command member creation input
     * @return created member
     */
    OrganizationMember add(AddOrganizationMemberCommand command);

    /**
     * Changes a member role.
     *
     * @param command role update input
     * @return updated member
     */
    OrganizationMember update(UpdateOrganizationMemberCommand command);

    /**
     * Removes a member without deleting the Identity.
     *
     * @param command member removal input
     */
    void remove(RemoveOrganizationMemberCommand command);
}

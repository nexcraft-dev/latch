package dev.nexcraft.latch.controlplane.service.membership;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.core.identity.error.IdentityNotFoundException;
import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.MembershipRole;
import dev.nexcraft.latch.controlplane.core.membership.command.AddOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.RemoveOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.command.UpdateOrganizationMemberCommand;
import dev.nexcraft.latch.controlplane.core.membership.dto.MemberPage;
import dev.nexcraft.latch.controlplane.core.membership.dto.OrganizationMember;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipConflictException;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipForbiddenException;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipNotFoundException;
import dev.nexcraft.latch.controlplane.core.membership.error.MembershipValidationException;
import dev.nexcraft.latch.controlplane.core.membership.query.ListMembershipsQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.ListOrganizationMembersQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.MembershipPage;
import dev.nexcraft.latch.controlplane.core.membership.service.MembershipService;
import dev.nexcraft.latch.controlplane.core.organization.error.OrganizationNotFoundException;
import dev.nexcraft.latch.controlplane.repository.identity.IdentityRepository;
import dev.nexcraft.latch.controlplane.repository.membership.OrganizationMembershipRepository;
import dev.nexcraft.latch.controlplane.repository.organization.OrganizationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Applies initial Organization membership and role authorization rules.
 */
public final class MembershipServiceImpl implements MembershipService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final IdentityRepository identityRepository;
    private final Clock clock;

    /**
     * Creates the membership service implementation.
     *
     * @param organizationRepository Organization persistence boundary
     * @param membershipRepository membership persistence boundary
     * @param identityRepository Identity persistence boundary
     * @param clock clock used for membership timestamps
     */
    public MembershipServiceImpl(
            OrganizationRepository organizationRepository,
            OrganizationMembershipRepository membershipRepository,
            IdentityRepository identityRepository,
            Clock clock) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.identityRepository = identityRepository;
        this.clock = clock;
    }

    /**
     * Lists members visible to the authenticated caller.
     *
     * @param query member listing input
     * @return member page
     */
    @Override
    public MemberPage list(ListOrganizationMembersQuery query) {
        MemberListInput input = memberListInput(query);
        requireOrganizationAccess(input.actorIdentityId(), input.organizationId());
        MembershipPage page = membershipRepository.findByOrganization(
                new ListMembershipsQuery(input.organizationId(), input.page(), input.size()));
        List<OrganizationMember> members = page.items().stream().map(this::toMember).toList();
        return new MemberPage(members, page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    /**
     * Adds a member after applying role-management rules.
     *
     * @param command member creation input
     * @return created member
     */
    @Override
    public OrganizationMember add(AddOrganizationMemberCommand command) {
        validateAddCommand(command);
        Membership actor = requireManager(command.actorIdentityId(), command.organizationId());
        if (actor.role() == MembershipRole.ADMIN && command.role() == MembershipRole.OWNER) {
            throw new MembershipForbiddenException("ADMIN cannot assign the OWNER role");
        }
        Identity identity = identityRepository.findById(command.identityId())
                .orElseThrow(() -> new IdentityNotFoundException(command.identityId()));
        if (membershipRepository.findByOrganizationAndIdentity(command.organizationId(), command.identityId()).isPresent()) {
            throw new MembershipConflictException("Identity is already a member of this Organization");
        }
        Instant now = Instant.now(clock);
        Membership membership = Membership.create(
                UUID.randomUUID(), command.organizationId(), identity.id(), command.role(), now);
        return toMember(membershipRepository.save(membership));
    }

    /**
     * Updates a member role after applying role-management rules.
     *
     * @param command role update input
     * @return updated member
     */
    @Override
    public OrganizationMember update(UpdateOrganizationMemberCommand command) {
        validateUpdateCommand(command);
        Membership actor = requireManager(command.actorIdentityId(), command.organizationId());
        Membership target = findTarget(command.organizationId(), command.membershipId());
        if (actor.role() == MembershipRole.ADMIN
                && (target.role() == MembershipRole.OWNER || command.role() == MembershipRole.OWNER)) {
            throw new MembershipForbiddenException("ADMIN cannot perform OWNER-level role changes");
        }
        return toMember(membershipRepository.save(target.changeRole(command.role(), Instant.now(clock))));
    }

    /**
     * Removes a membership row without removing the Identity.
     *
     * @param command member removal input
     */
    @Override
    public void remove(RemoveOrganizationMemberCommand command) {
        validateRemoveCommand(command);
        Membership actor = requireManager(command.actorIdentityId(), command.organizationId());
        Membership target = findTarget(command.organizationId(), command.membershipId());
        if (actor.role() == MembershipRole.ADMIN && target.role() == MembershipRole.OWNER) {
            throw new MembershipForbiddenException("ADMIN cannot remove an OWNER");
        }
        membershipRepository.delete(target);
    }

    private Membership requireManager(UUID actorIdentityId, UUID organizationId) {
        requireIdentifiers(actorIdentityId, organizationId);
        requireOrganizationAccess(actorIdentityId, organizationId);
        Membership actor = membershipRepository.findByOrganizationAndIdentity(organizationId, actorIdentityId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        if (actor.role() != MembershipRole.OWNER && actor.role() != MembershipRole.ADMIN) {
            throw new MembershipForbiddenException("Organization membership management requires OWNER or ADMIN role");
        }
        return actor;
    }

    private void requireOrganizationAccess(UUID actorIdentityId, UUID organizationId) {
        if (organizationRepository.findActiveByIdForIdentity(organizationId, actorIdentityId).isEmpty()) {
            throw new OrganizationNotFoundException(organizationId);
        }
    }

    private Membership findTarget(UUID organizationId, UUID membershipId) {
        return membershipRepository.findByOrganizationAndId(organizationId, membershipId)
                .orElseThrow(() -> new MembershipNotFoundException(membershipId));
    }

    private OrganizationMember toMember(Membership membership) {
        Identity identity = identityRepository.findById(membership.identityId())
                .orElseThrow(() -> new IdentityNotFoundException(membership.identityId()));
        return new OrganizationMember(
                membership.id(),
                membership.organizationId(),
                membership.identityId(),
                identity.email(),
                identity.displayName(),
                membership.role(),
                membership.createdAt(),
                membership.updatedAt());
    }

    private MemberListInput memberListInput(ListOrganizationMembersQuery query) {
        if (query == null) {
            throw new MembershipValidationException("Membership list query is required");
        }
        requireIdentifiers(query.actorIdentityId(), query.organizationId());
        int page = query.page() == null ? DEFAULT_PAGE : query.page();
        int size = query.size() == null ? DEFAULT_SIZE : query.size();
        validatePage(page, size);
        return new MemberListInput(query.actorIdentityId(), query.organizationId(), page, size);
    }

    private void validateAddCommand(AddOrganizationMemberCommand command) {
        if (command == null) {
            throw new MembershipValidationException("Add member command is required");
        }
        requireIdentifiers(command.actorIdentityId(), command.organizationId());
        if (command.identityId() == null) {
            throw new MembershipValidationException("Member Identity id is required");
        }
        if (command.role() == null) {
            throw new MembershipValidationException("Member role is required");
        }
    }

    private void validateUpdateCommand(UpdateOrganizationMemberCommand command) {
        if (command == null) {
            throw new MembershipValidationException("Update member command is required");
        }
        requireIdentifiers(command.actorIdentityId(), command.organizationId());
        if (command.membershipId() == null) {
            throw new MembershipValidationException("Membership id is required");
        }
        if (command.role() == null) {
            throw new MembershipValidationException("Member role is required");
        }
    }

    private void validateRemoveCommand(RemoveOrganizationMemberCommand command) {
        if (command == null) {
            throw new MembershipValidationException("Remove member command is required");
        }
        requireIdentifiers(command.actorIdentityId(), command.organizationId());
        if (command.membershipId() == null) {
            throw new MembershipValidationException("Membership id is required");
        }
    }

    private void requireIdentifiers(UUID actorIdentityId, UUID organizationId) {
        if (actorIdentityId == null) {
            throw new MembershipValidationException("Authenticated Identity is required");
        }
        if (organizationId == null) {
            throw new MembershipValidationException("Organization id is required");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new MembershipValidationException("page must be greater than or equal to zero");
        }
        if (size < 1 || size > 100) {
            throw new MembershipValidationException("size must be between 1 and 100");
        }
    }

    private record MemberListInput(UUID actorIdentityId, UUID organizationId, int page, int size) {
    }
}

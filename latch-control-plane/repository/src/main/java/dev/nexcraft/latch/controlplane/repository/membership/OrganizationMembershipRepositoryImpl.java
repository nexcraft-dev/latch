package dev.nexcraft.latch.controlplane.repository.membership;

import dev.nexcraft.latch.controlplane.core.membership.Membership;
import dev.nexcraft.latch.controlplane.core.membership.query.ListMembershipsQuery;
import dev.nexcraft.latch.controlplane.core.membership.query.MembershipPage;
import dev.nexcraft.latch.controlplane.repository.membership.persistence.OrganizationMembershipEntityMapper;
import dev.nexcraft.latch.controlplane.repository.membership.persistence.OrganizationMembershipPanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

/**
 * Panache-backed Organization membership repository.
 */
@ApplicationScoped
public class OrganizationMembershipRepositoryImpl implements OrganizationMembershipRepository {

    private final OrganizationMembershipPanacheRepository repository;
    private final OrganizationMembershipEntityMapper mapper;

    /**
     * Creates the Organization membership repository.
     *
     * @param repository Panache access object
     */
    public OrganizationMembershipRepositoryImpl(OrganizationMembershipPanacheRepository repository) {
        this.repository = repository;
        this.mapper = new OrganizationMembershipEntityMapper();
    }

    @Override
    public Optional<Membership> findByOrganizationAndId(UUID organizationId, UUID membershipId) {
        return repository.find("organizationId = ?1 and id = ?2", organizationId, membershipId)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Membership> findByOrganizationAndIdentity(UUID organizationId, UUID identityId) {
        return repository.find("organizationId = ?1 and identityId = ?2", organizationId, identityId)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public MembershipPage findByOrganization(ListMembershipsQuery query) {
        String where = "organizationId = ?1";
        Object[] parameters = {query.organizationId()};
        long totalElements = repository.count(where, parameters);
        var memberships = repository.find(
                        where,
                        Sort.by("createdAt").ascending().and("id", Sort.Direction.Ascending),
                        parameters)
                .page(Page.of(query.page(), query.size()))
                .list()
                .stream()
                .map(mapper::toDomain)
                .toList();
        return new MembershipPage(memberships, query.page(), query.size(), totalElements);
    }

    @Override
    @Transactional
    public Membership save(Membership membership) {
        var entity = repository.findById(membership.id());
        if (entity == null) {
            entity = mapper.toEntity(membership);
            repository.persist(entity);
        } else {
            entity.apply(membership);
        }
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void delete(Membership membership) {
        repository.deleteById(membership.id());
    }
}

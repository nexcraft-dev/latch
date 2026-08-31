package dev.nexcraft.latch.controlplane.repository.identity;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import dev.nexcraft.latch.controlplane.repository.identity.persistence.IdentityEntity;
import dev.nexcraft.latch.controlplane.repository.identity.persistence.IdentityEntityMapper;
import dev.nexcraft.latch.controlplane.repository.identity.persistence.IdentityPanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

/**
 * Panache-backed Identity repository.
 */
@ApplicationScoped
public class IdentityRepositoryImpl implements IdentityRepository {

    private final IdentityPanacheRepository repository;
    private final IdentityEntityMapper mapper;

    /**
     * Creates the Identity repository.
     *
     * @param repository Panache access object
     */
    public IdentityRepositoryImpl(IdentityPanacheRepository repository) {
        this.repository = repository;
        this.mapper = new IdentityEntityMapper();
    }

    @Override
    public Optional<Identity> findByProviderAndSubject(String provider, String providerSubject) {
        return repository.find("provider = ?1 and providerSubject = ?2", provider, providerSubject)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Identity> findById(UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Identity save(Identity identity) {
        IdentityEntity entity = repository.findById(identity.id());
        if (entity == null) {
            entity = mapper.toEntity(identity);
            repository.persist(entity);
        } else {
            entity.apply(identity);
        }
        return mapper.toDomain(entity);
    }
}

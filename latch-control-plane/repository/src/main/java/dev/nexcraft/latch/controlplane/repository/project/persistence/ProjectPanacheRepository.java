package dev.nexcraft.latch.controlplane.repository.project.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Panache access object kept inside the Project persistence adapter.
 */
@ApplicationScoped
public class ProjectPanacheRepository implements PanacheRepositoryBase<ProjectEntity, UUID> {
}

package dev.nexcraft.latch.controlplane.repository.identity.persistence;

import dev.nexcraft.latch.controlplane.core.identity.Identity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of an externally authenticated Identity.
 */
@Entity
@Table(name = "identities")
public class IdentityEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "provider", nullable = false, length = 100)
    private String provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Creates an empty entity for JPA.
     */
    protected IdentityEntity() {
    }

    /**
     * Creates an entity with an Identity identifier.
     *
     * @param id Identity identifier
     */
    public IdentityEntity(UUID id) {
        this.id = id;
    }

    /**
     * Returns the Identity identifier.
     *
     * @return Identity identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the external provider.
     *
     * @return provider issuer
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the external subject.
     *
     * @return provider subject
     */
    public String getProviderSubject() {
        return providerSubject;
    }

    /**
     * Returns the optional profile email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the optional display name.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the last update timestamp.
     *
     * @return update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Applies an Identity to the persistence row.
     *
     * @param identity source Identity
     */
    public void apply(Identity identity) {
        this.provider = identity.provider();
        this.providerSubject = identity.providerSubject();
        this.email = identity.email();
        this.displayName = identity.displayName();
        this.createdAt = identity.createdAt();
        this.updatedAt = identity.updatedAt();
    }
}

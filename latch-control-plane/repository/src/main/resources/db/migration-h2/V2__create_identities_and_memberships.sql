CREATE TABLE identities (
    id UUID PRIMARY KEY,
    provider VARCHAR(100) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    display_name VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_identities_provider_subject
        UNIQUE (provider, provider_subject)
);

CREATE TABLE organization_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    identity_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_memberships_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_memberships_identity
        FOREIGN KEY (identity_id)
        REFERENCES identities(id),

    CONSTRAINT uq_memberships_organization_identity
        UNIQUE (organization_id, identity_id),

    CONSTRAINT chk_memberships_role
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER'))
);

CREATE INDEX idx_memberships_identity_organization
    ON organization_memberships (identity_id, organization_id);

CREATE INDEX idx_memberships_organization_role
    ON organization_memberships (organization_id, role, identity_id);

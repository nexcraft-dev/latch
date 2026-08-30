CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_organizations_slug UNIQUE (slug),
    CONSTRAINT chk_organizations_status
        CHECK (status IN ('ACTIVE', 'DELETED'))
);

CREATE INDEX idx_organizations_active_created_at
    ON organizations (created_at, id);

CREATE INDEX idx_organizations_active_updated_at
    ON organizations (updated_at, id);

CREATE INDEX idx_organizations_active_name
    ON organizations (name, id);

CREATE INDEX idx_organizations_active_slug
    ON organizations (slug, id);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    "key" VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_projects_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT uq_projects_organization_key
        UNIQUE (organization_id, "key"),

    CONSTRAINT chk_projects_status
        CHECK (status IN ('ACTIVE', 'DELETED'))
);

CREATE INDEX idx_projects_active_organization_created_at
    ON projects (organization_id, created_at DESC, id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_projects_active_organization_updated_at
    ON projects (organization_id, updated_at DESC, id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_projects_active_organization_name
    ON projects (organization_id, LOWER(name), id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_projects_active_organization_key
    ON projects (organization_id, LOWER("key"), id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_projects_active_organization_description
    ON projects (organization_id, LOWER(description), id)
    WHERE status = 'ACTIVE';

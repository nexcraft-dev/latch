# Latch

Latch is an open-source feature flag platform built for self-hosting and the cloud.

## Backend build

The repository uses a Gradle composite build. The root build aggregates independently buildable backend projects, while `latch-control-plane` is a multi-project Gradle build.

```text
web → service → repository → core
```

Module responsibilities:

- `core`: framework-free organization, Identity, and membership models, DTOs,
  service interfaces, query values, and shared exceptions.
- `service`: service implementations, organization slug/pagination policy, and
  membership authorization rules.
- `repository`: co-located repository interfaces and `Impl` classes, database
  entities, explicit mappers, and Flyway migrations for organizations,
  Identities, and memberships.
- `web`: handwritten controllers, the verified OIDC-to-Identity adapter,
  request-scoped current Identity, exception handlers, and Quarkus HTTP
  Problem integration.

The layer flow is explicit: the controller calls the service interface, the
service implementation calls the repository interface, and the repository uses
Panache. Request/response DTOs are handwritten in `core` and imported by `web`;
they are not generated from OpenAPI. Controllers never expose persistence
entities or JWT objects.

Shared Gradle settings are defined by the `latch.settings-conventions` plugin
in `build-logic`. It owns plugin repositories, dependency repositories, the
`FAIL_ON_PROJECT_REPOS` policy, and the shared version catalog. The root build
uses Gradle's automatic root catalog, while independent child builds resolve
the same catalog from the repository root.

## Versions

- Java: 25
- Gradle Wrapper: 9.4.1
- Quarkus: 3.39.1

## Commands

Run commands from the repository root:

```bash
./gradlew projects
./gradlew clean spotlessApply checkstyleTest checkstyleMain test build componentTest
./gradlew build
./gradlew test
./gradlew check
./gradlew componentTest
```

The child build can also be run independently:

```bash
cd latch-control-plane
./gradlew build
```

The Quarkus health endpoint is available at `/q/health` when the `web` application is running. It is a framework-managed endpoint, not a product REST API. Future product APIs must define an OpenAPI contract first and use versioned `/api/v1/...` routes.

For local Quarkus development, leave the datasource connection unset. Quarkus
Dev Services will start a PostgreSQL container automatically when Docker or
OrbStack is running, then Flyway applies the migrations. The application keeps
explicit OIDC and JDBC settings under the `prod` profile for deployed
environments. With no local OIDC URL configured, Quarkus also starts its
Keycloak Dev Service automatically for development-mode authentication.

If a local PostgreSQL instance is already running, provide its connection
through Quarkus configuration and keep Dev Services from taking over:

```bash
cd latch-control-plane
QUARKUS_DATASOURCE_DEVSERVICES_ENABLED=false \
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/latch \
QUARKUS_DATASOURCE_USERNAME=latch \
QUARKUS_DATASOURCE_PASSWORD=latch \
./gradlew :web:quarkusDev
```

If Docker/OrbStack is not available, start PostgreSQL manually or use the
explicit connection settings above. A `Connection refused` error for
`localhost:5432` means no PostgreSQL server is listening at that address.

If an external OIDC URL is provided while running dev mode, that provider must
be reachable. Otherwise leave `OIDC_AUTH_SERVER_URL` unset so the local
Keycloak Dev Service can provide the development provider.

## Authentication and membership

Product endpoints require a bearer token issued by the configured external
OIDC provider. Latch does not store passwords or implement its own login
session. Configure the deployment with:

```bash
OIDC_AUTH_SERVER_URL=http://localhost:8180/realms/latch
OIDC_CLIENT_ID=latch-control-plane
OIDC_ISSUER=https://issuer.example/realms/latch
OIDC_AUDIENCE=latch-control-plane
```

The verified OIDC `iss` and `sub` claims identify an Identity. Email and name
are profile data only; email is never used as the external identity key.

The current Identity is available at `GET /api/v1/me`. Organization access is
membership-scoped: creating an organization atomically creates the caller's
`OWNER` membership, and deleted organizations remain inaccessible. Membership
roles are `OWNER`, `ADMIN`, `MEMBER`, and `VIEWER`; only owners and admins can
manage members, and admins cannot perform owner-level operations.

The membership endpoints are:

```text
GET    /api/v1/organizations/{organizationId}/members
POST   /api/v1/organizations/{organizationId}/members
PATCH  /api/v1/organizations/{organizationId}/members/{membershipId}
DELETE /api/v1/organizations/{organizationId}/members/{membershipId}
```

The checked-in contract is
`latch-control-plane/web/src/main/openapi/organization.yaml`. It documents
the bearer security scheme, Identity, membership, pagination, and RFC 7807
error responses. `/q/health` remains framework-managed and is not part of
the product REST contract.

JVM API tests use Quarkus' deterministic `@TestSecurity` and `@OidcSecurity`
fixtures, so they do not require a live OIDC provider. `componentTest` starts
the packaged application with OIDC disabled in its isolated test profile and
checks packaged health and product-endpoint rejection; live-provider token
exchange is deployment verification.

## Organization API

The initial product API manages organizations under `/api/v1/organizations`. The
OpenAPI source contract is at
`latch-control-plane/web/src/main/openapi/organization.yaml`. It is a checked-in
contract and documentation artifact. The Java controller and DTO/model classes
are handwritten in `web` and `core` respectively; OpenAPI Generator is not used
by the build.

```bash
curl -i -X POST http://localhost:8080/api/v1/organizations \
  -H "Authorization: Bearer $OIDC_ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Acme Corporation"}'

curl -H "Authorization: Bearer $OIDC_ACCESS_TOKEN" \
  'http://localhost:8080/api/v1/organizations?search=acme&page=0&size=20&sort=name,asc'
```

Organization names are required, non-blank, and limited to 120 characters.
Slugs are generated by the server, are unique across active and deleted rows,
and are limited to 80 characters. List results contain active organizations
only. Supported sort fields are `name`, `slug`, `createdAt`, and `updatedAt`;
directions are `asc` and `desc`. The default is `createdAt,desc`, with a default
page size of 20 and a maximum of 100. Deletes are soft deletes and make the
organization unavailable to get, update, or list operations.

Invalid requests return `application/problem+json` responses through the
Quarkiverse `quarkus-http-problem` extension. Standard parsing/validation errors
use its built-in mappers; the web layer only adapts domain exceptions. `/q/health`
is framework-managed and is not part of the product REST contract.

If Gradle cannot write its default native cache in the local environment, use a writable cache location:

```bash
GRADLE_USER_HOME=/tmp/latch-gradle-home ./gradlew build
```

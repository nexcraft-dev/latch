# Project Overview

Latch Control Plane is the management plane for the Latch feature flag
platform.

Latch is an open-source, self-hostable feature flag platform that can also
be operated as a managed SaaS.

The Control Plane is responsible for managing:

- Organizations
- Projects
- Environments
- Feature flags
- Variants
- Targeting rules
- Segments
- SDK keys
- Service accounts
- Membership and RBAC

The Control Plane is the source of truth for feature flag configuration.
It does not perform feature evaluation for application requests.

## Technology

- Java 25
- Quarkus
- PostgreSQL
- Hibernate ORM with Panache
- Flyway
- Kafka
- GraalVM Native Image
- Gradle

## Architecture

Use Clean Architecture with clear dependency boundaries.

Domain code must not depend on Quarkus, PostgreSQL, Kafka, REST,
or other infrastructure concerns.

Preferred dependency direction:

infrastructure -> application -> domain

Main modules:

- organization
- project
- environment
- feature
- segment
- identity
- authorization
- outbox

## Native Image

GraalVM Native Image compatibility is a hard requirement.

- Avoid runtime reflection where possible.
- Avoid runtime classpath scanning.
- Avoid dynamic proxies unless required and verified.
- New dependencies must be checked for Native Image compatibility.
- Native build failures must not be ignored.

## Technology

- Java 25
- Quarkus
- PostgreSQL
- Hibernate ORM with Panache
- Flyway
- Kafka
- GraalVM Native Image
- Gradle

## Persistence

PostgreSQL is the source of truth.

- Use Hibernate ORM with Panache for database access.
- Prefer the Repository Pattern over the Active Record Pattern.
- Do not extend `PanacheEntity` or `PanacheEntityBase` from domain models.
- Keep persistence entities separate from domain models.
- Keep Panache and JPA-specific concerns within the persistence layer.
- Schema changes must use Flyway migrations.
- Do not expose persistence entities outside the persistence layer.
- Domain models must not depend on Panache, Hibernate ORM, or JPA.
- Map explicitly between persistence entities and domain models.

## Events

Feature configuration changes must use the transactional outbox pattern.

Never perform:

1. database commit
2. Kafka publish

as two independent operations.

Persist the domain change and outbox event in the same database
transaction.

## Do Not

- Do not introduce Spring dependencies.
- Do not introduce Hibernate/JPA.
- Do not create a microservice for each domain entity.
- Do not access PostgreSQL from domain code.
- Do not publish Kafka events directly from REST resources.
- Do not add Redis unless explicitly required.
- Do not add Lombok.
- Do not change architectural boundaries without discussing it first.

## Definition of Done

Before considering a change complete:

1. Unit tests must pass.
2. Integration tests must pass.
3. Formatting/static analysis must pass.
4. The application must compile on Java 25.
5. Native Image compatibility must not be broken.
6. Database changes must include Flyway migrations.
7. Public API changes must be documented.

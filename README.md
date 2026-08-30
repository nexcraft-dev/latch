# Latch

Latch is an open-source feature flag platform built for self-hosting and the cloud.

## Backend build

The repository uses a Gradle composite build. The root build aggregates independently buildable backend projects, while `latch-control-plane` is a multi-project Gradle build.

```text
web → service → repository → core
```

Module responsibilities:

- `core`: framework-free contracts, future service/client interfaces, DTOs, and shared exceptions.
- `service`: application service implementations.
- `repository`: repository implementations and framework-independent domain classes.
- `web`: Quarkus entry point, controllers, handlers, and filters.

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

If Gradle cannot write its default native cache in the local environment, use a writable cache location:

```bash
GRADLE_USER_HOME=/tmp/latch-gradle-home ./gradlew build
```

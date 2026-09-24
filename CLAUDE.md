# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.5.6 (Java 21) backend for **LOST-INHA**, 인하대학교 캠퍼스 분실물/습득물 통합 관리 플랫폼. This repo (`BE`) is the backend only — the web admin frontend and the student mobile app (React / React Native + Expo) live in separate repos (`ADMIN`, `FE`).

## Commands

```bash
./gradlew build          # compile + run tests + assemble jar
./gradlew bootRun         # run the app locally (default port 8080)
./gradlew test            # run all tests
./gradlew test --tests "NotFound.next_campus.NextCampusApplicationTests"   # run a single test class
```

There is currently only one test file (`NextCampusApplicationTests`, the Gradle-generated context load test) — no domain-level unit/integration tests exist yet.

### Local setup

`src/main/resources/application.properties` and `application-local.properties` are gitignored (contain DB/JWT/mail/Firebase secrets) and must be created locally. Required env vars referenced from `application.properties`: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION`, `FIREBASE_CONFIG_PATH`, `MAIL_USERNAME`, `MAIL_PASSWORD`. The datasource currently points at MySQL (`spring.jpa.properties.hibernate.dialect=MySQLDialect`); `spring.jpa.hibernate.ddl-auto=none` — schema is managed manually via `sql/ddl.sql` / `sql/dml.sql` / `sql/init.sql`, not by Hibernate auto-generation.

## Architecture

### Domain-per-package layout

Code is organized by business domain under `domain/`, each following the same internal layering:

```
domain/<name>/
  api/        # @RestController
  dto/        # request/, response/ subpackages
  model/      # JPA @Entity
  repository/ # Spring Data JPA
  service/    # business logic
```

Domains: `category`, `comment`, `location`, `member`, `notification`, `post`, `receiver`.

**Service layer is inconsistent by design across domains** — `post`, `comment`, `notification`, `receiver` split into a `Service` interface + `ServiceImpl`; `category` and `location` implement business logic directly in a single `@Service`-annotated class with no interface. When touching a domain, match its existing pattern rather than introducing a new one.

`member` has no `service/` package — member-related auth logic lives in `global/auth` instead (see below), not in `domain/member`.

### `post` is the central domain

`Post` is the lost-and-found item entity. It `@ManyToOne`s to `Member` and `Location`. Categories are many-to-many through the join entity `PostCategory` (not a direct `@ManyToMany`), and images are one-to-many through `PostImage`. `sql/ddl.sql` documents the migration history of this schema (e.g. `item` table renamed to `post_category`, FK cascade changes) — check it before assuming current column names/constraints.

### `global/` — cross-cutting concerns

- `global/auth/token/` — JWT issuing/validation (`JwtTokenProvider`), `TokenController` (login/refresh), `MemberAuthService` (Spring Security `UserDetailsService` implementation backed by `member` domain), `RestExceptionHandler` for token-related exceptions only (**not** a project-wide `@ControllerAdvice` — other domains don't get centralized exception handling yet).
- `global/auth/user/` — `CustomUserDetails` wraps `Member` + `Role` (`USER`/`ADMIN`) for Spring Security.
- `global/config/auth/SecurityConfig` — stateless JWT security filter chain. Only `GET /posts/**`, `/comments/**`, `/categories/**`, `/locations/**`, `/uploads/**`, and `/auth/login`, `/auth/refresh` are `permitAll`; everything else requires authentication. Role checks (e.g. `ADMIN`-only category mutation) are done manually inside service methods via `CustomUserDetails.getMember().getRole()`, not via `@PreAuthorize`.
- `global/config/web/WebConfig` — CORS allowlist (`localhost:3000`, `lost-inha.kro.kr`) and serves uploaded files from `/uploads/**` mapped to a local disk path (`user.dir/uploads/`) — this is local-filesystem storage, not object storage, so it won't survive redeploys on ephemeral hosts.
- `global/firebase/` — FCM push notifications (`FcmService`, `FirebaseMessagingService`) plus `ExpoPushService` for Expo-based push (mobile app uses Expo). `FirebaseConfig` loads credentials from a file path (`fcm.firebase.config.path`), not an env var.
- `global/mail/` — Gmail SMTP notification emails.

### Auth flow

`JwtAuthenticationFilter` (registered before `UsernamePasswordAuthenticationFilter` in `SecurityConfig`) reads the token from the `Authorization: Bearer` header first, falling back to an `ACCESS_TOKEN` cookie. Token subject is the student ID, resolved to a `CustomUserDetails` via `MemberAuthService`.

## Known gaps (relevant when asked to improve the codebase)

- No project-wide `@RestControllerAdvice` / unified response envelope — error handling and response shape differ per controller.
- No test coverage beyond the Spring context load test.
- File uploads go to local disk, not object storage.
- DB is MySQL; a move to PostgreSQL is planned (see `docs/DEVELOPMENT_PLAN.md`) for Render deployment, since Render doesn't offer managed MySQL.

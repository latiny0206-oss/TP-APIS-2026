# AGENTS Guide - TP-APIS-2026

## Scope and Current State
- Workspace root contains a single Spring Boot service at `ecommerce/ecommerce`.
- Current codebase is a bootstrap skeleton (entrypoint + context test), so prioritize safe incremental scaffolding over refactors.
- Existing AI guidance sources found via glob: `README.md` only (no `.github/copilot-instructions.md`, `AGENT.md`, `CLAUDE.md`, etc.).

## Architecture Snapshot
- Main app class: `ecommerce/ecommerce/src/main/java/com/trekking/ecommerce/EcommerceApplication.java`.
- Package root is `com.trekking.ecommerce`; keep new classes under this namespace to stay inside component scan.
- No explicit layers yet (`controller/service/repository` absent). Add features using standard Spring layering to avoid ad-hoc package growth.
- Configuration lives in `ecommerce/ecommerce/src/main/resources/application.properties`.

## Data and Security Integration Points
- Database target is MySQL (`spring.datasource.url` points to `trekking_ecommerce` with `createDatabaseIfNotExist=true`).
- JPA strategy is `spring.jpa.hibernate.ddl-auto=update`; schema changes happen implicitly on startup.
- Security/JWT are expected by config and dependencies (`spring-boot-starter-security`, `jwt.secret`, `jwt.expiration`).
- API docs are expected through Springdoc paths (`/api-docs`, `/swagger-ui.html`).
- File uploads are enabled with multipart limits (10MB file, 30MB request).

## Build/Test Workflows (Observed)
- Project uses Maven wrapper scripts in `ecommerce/ecommerce/mvnw` and `ecommerce/ecommerce/mvnw.cmd`.
- On this workspace snapshot, `./mvnw` is not executable (`permission denied`), so first run may require `chmod +x mvnw`.
- System Maven is not available here (`mvn: command not found`), so rely on wrapper when running locally.
- Baseline test is `ecommerce/ecommerce/src/test/java/com/trekking/ecommerce/EcommerceApplicationTests.java` and only checks Spring context load.

## Project-Specific Conventions To Preserve
- Keep comments/properties in Spanish where already present (example: `# Base de datos`, `# Subida de fotos`).
- Keep Java version at 17 unless `pom.xml` is intentionally updated.
- Lombok is configured as annotation processor and excluded from final artifact; avoid committing generated boilerplate.
- There are two `spring.application.name` keys in `application.properties`; later one (`trekking-ecommerce`) overrides earlier one.

## Agent Operating Rules For This Repo
- Treat this as a greenfield foundation: add minimal vertical slices (entity -> repository -> service -> controller) with runnable increments.
- When adding endpoints, align with existing integrations: validation (`spring-boot-starter-validation`), security, and Swagger visibility.
- If changing datasource/auth config, update both code and `application.properties` together to keep startup predictable.
- Prefer small commits that keep `EcommerceApplicationTests` passing and add focused tests per new package.


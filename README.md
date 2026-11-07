# GymHub SA — API

Backend REST API for **GymHub SA**, a gym membership management system. Provides authentication, member, membership, attendance, payment, trainer, workout, diet & nutrition, loyalty, notification, dashboard, and reporting services consumed by the Angular web app (admin/trainer) and the React Native mobile app (member).

## Tech Stack
Java 21 · Spring Boot 3.5 · Spring Security (JWT) · Spring Data JPA · Maven · PostgreSQL (prod) · H2 (dev/test) · Flyway · Swagger/OpenAPI (springdoc) · JUnit 5

## Status
🚧 In active development (MVP) — **Auth module** implemented (login, refresh, logout, forgot/reset password, JWT + RBAC)

## Getting Started

**Prerequisites:** JDK 21, Maven 3.9+

```bash
git clone https://github.com/Mabelebele/gymhub-sa-api.git
cd gymhub-sa-api
./mvnw spring-boot:run
```

The app starts on `http://localhost:8081` using the `dev` profile (H2, in-memory) by default — no extra setup required. On first run it seeds a default admin account:

```
email:    admin@gymhubsa.co.za
password: Admin@12345
```

## Running Locally

Profiles are controlled via `SPRING_PROFILES_ACTIVE` (defaults to `dev`):

| Profile | Database | Notes |
|---|---|---|
| `dev` | H2 (in-memory) | SQL logging on, H2 console at `/h2-console`, seeds a default admin |
| `prod` | PostgreSQL | For running locally against a real Postgres instance — requires `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` env vars |

Copy [.env.example](.env.example) to `.env` (or export the variables directly) and adjust as needed — see that file for the full list, including `JWT_SECRET`, token expirations, and `CORS_ALLOWED_ORIGINS`.

Sample request bodies for every endpoint are in [samples/](samples/), one JSON file per request DTO.

```bash
# dev (default)
./mvnw spring-boot:run

# prod-style run locally
SPRING_PROFILES_ACTIVE=prod SPRING_DATASOURCE_URL=... ./mvnw spring-boot:run
```

Schema is managed by Flyway migrations in `src/main/resources/db/migration`, applied automatically on startup against both H2 and PostgreSQL.

## API Documentation

Once running, Swagger UI is available at `http://localhost:8081/swagger-ui.html` (raw OpenAPI JSON at `/v3/api-docs`). Use the "Authorize" button with a bearer token from `/api/v1/auth/login` to call protected endpoints.

### Auth endpoints (implemented so far)

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Authenticate, returns access + refresh JWT |
| POST | `/api/v1/auth/refresh` | Exchange a refresh token for a new token pair |
| POST | `/api/v1/auth/logout` | Clears server-side security context (tokens are stateless — client must discard them) |
| POST | `/api/v1/auth/forgot-password` | Issues a password reset token (currently logged, not emailed — notification module pending) |
| POST | `/api/v1/auth/reset-password` | Resets password using a valid reset token |

## Testing

```bash
./mvnw test
```

Covers service-layer unit tests (Mockito), `@DataJpaTest` repository tests (H2), and full `@SpringBootTest` + MockMvc integration tests for the Auth endpoints.

## Deployment
_(Not applicable — this is a local/educational project; the API is intended to be run locally with the `dev` (H2) or `prod` (local Postgres) profile.)_

## Related Repositories
- [gymhub-sa-web](https://github.com/Mabelebele/gymhub-sa-web) — Angular admin/trainer portal
- [gymhub-sa-mobile](https://github.com/Mabelebele/gymhub-sa-mobile) — React Native member app


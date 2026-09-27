# Expense Tracker Application

A full-stack expense management system to track and analyze user financial behavior with real-time insights.

- **Java 17** · **Spring Boot 3** · **Spring Security + JWT** · **MySQL**
- Secured REST endpoints (JWT auth, row-level ownership checks)
- Category-based budgeting with utilization tracking
- Analytics endpoints for dashboards (income/expense summary, category breakdown, monthly trend, discretionary-spend insight)

This backend implements the design in `/docs` (SRS, architecture, DB design, API spec, LLD, test plan).

---

## Tech Stack

| Layer | Tech |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security 6 + JJWT |
| Persistence | Spring Data JPA (Hibernate) |
| Database | MySQL 8 |
| Build | Maven |
| API Docs | springdoc-openapi (Swagger UI) |

---

## Prerequisites
- JDK 17+
- Maven 3.9+ (or use the included wrapper once generated — see below)
- MySQL 8 running locally, or via Docker

## 1. Set up MySQL

```bash
docker run --name expense-tracker-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=expense_tracker \
  -p 3306:3306 -d mysql:8
```

Or point it at any MySQL instance you already have — just set the env vars below.

## 2. Configure environment variables

The app reads these (with dev-friendly defaults in `application.yml`):

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=expense_tracker
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=replace-with-a-long-random-string-at-least-32-chars
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGIN=http://localhost:3000
```

Hibernate is set to `ddl-auto: update`, so tables are created automatically on first run. `src/main/resources/schema-reference.sql` is provided if you'd rather run the DDL manually.

## 3. Run the app

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`, with Swagger UI at `http://localhost:8080/swagger-ui.html`.

## 4. Try it

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Karthik R","email":"karthik@example.com","password":"SecurePass123!"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"karthik@example.com","password":"SecurePass123!"}'

# Use the returned accessToken for everything else
curl http://localhost:8080/api/categories \
  -H "Authorization: Bearer <accessToken>"
```

---

## Project Structure

```
src/main/java/com/expensetracker
├── config          # Security & OpenAPI configuration
├── security        # JWT provider, filter, UserDetails
├── entity          # JPA entities
├── repository      # Spring Data repositories + specifications
├── dto             # Request/response DTOs
├── exception       # Custom exceptions + global handler
├── service         # Business logic
└── controller      # REST controllers
```

## Full API reference
See `04_API_Specification_Document.md` in the project docs for every endpoint, request/response shape, and error format.

## Running tests
```bash
mvn test
```
`ExpenseTrackerApplicationTests` verifies the Spring context loads (requires a reachable MySQL instance).

---

## Roadmap
- Multi-currency support
- Recurring transactions
- Budget threshold email/SMS alerts
- CSV/bank import

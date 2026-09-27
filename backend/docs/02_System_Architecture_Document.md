# System Architecture Document
## Expense Tracker Application

**Version:** 1.0

---

## 1. Architecture Style
Layered (N-tier) monolithic architecture — appropriate for a single-team, portfolio-scale project. Clean separation of concerns allows easy extraction into microservices later if needed.

```
┌─────────────────────────────────────────────────────────┐
│                      Client (Browser)                    │
│              React / Angular SPA (or Thymeleaf)           │
└───────────────────────────┬───────────────────────────────┘
                            │ HTTPS (REST/JSON + JWT)
┌───────────────────────────▼───────────────────────────────┐
│                    Spring Boot Application                │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │  Security Filter Chain (Spring Security + JWT)      │   │
│  └────────────────────────────────────────────────────┘   │
│  ┌────────────────────────────────────────────────────┐   │
│  │  Controller Layer (REST Controllers)                │   │
│  │  AuthController, TransactionController,             │   │
│  │  CategoryController, BudgetController,               │   │
│  │  DashboardController                                 │   │
│  └────────────────────────────────────────────────────┘   │
│  ┌────────────────────────────────────────────────────┐   │
│  │  Service Layer (Business Logic)                     │   │
│  │  AuthService, TransactionService, BudgetService,     │   │
│  │  AnalyticsService                                    │   │
│  └────────────────────────────────────────────────────┘   │
│  ┌────────────────────────────────────────────────────┐   │
│  │  Repository Layer (Spring Data JPA)                 │   │
│  │  UserRepository, TransactionRepository,              │   │
│  │  CategoryRepository, BudgetRepository                │   │
│  └────────────────────────────────────────────────────┘   │
└───────────────────────────┬───────────────────────────────┘
                            │ JDBC
┌───────────────────────────▼───────────────────────────────┐
│                      MySQL Database                        │
│   users | categories | transactions | budgets              │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Component Responsibilities

### 2.1 Security Layer
- `JwtAuthenticationFilter` — intercepts requests, validates JWT, sets `SecurityContext`.
- `JwtTokenProvider` — generates/validates/parses tokens (signing key, expiry).
- `SecurityConfig` — defines public vs. protected routes, password encoder (BCrypt), CORS policy.
- `CustomUserDetailsService` — loads user for Spring Security.

### 2.2 Controller Layer
Thin layer: request validation (`@Valid`), delegates to services, maps to HTTP responses via DTOs. Never touches entities directly.

### 2.3 Service Layer
Contains business rules:
- Ownership checks (a user can only touch their own records).
- Budget utilization calculations.
- Analytics aggregation (grouping, percentage calculations).

### 2.4 Repository Layer
Spring Data JPA interfaces extending `JpaRepository`, with custom `@Query` methods for aggregation queries (e.g., sum of expenses by category/month).

### 2.5 Exception Handling
Global `@ControllerAdvice` (`GlobalExceptionHandler`) maps exceptions (validation, not-found, unauthorized, duplicate email) to consistent JSON error responses with correct HTTP status codes.

---

## 3. Request Flow Example (Create Transaction)

1. Client sends `POST /api/transactions` with JWT in `Authorization` header and transaction JSON body.
2. `JwtAuthenticationFilter` validates token, sets authenticated user in context.
3. `TransactionController` validates request DTO, calls `TransactionService.createTransaction()`.
4. `TransactionService` resolves current user (from `SecurityContext`), validates category ownership, persists via `TransactionRepository`.
5. Response mapped to `TransactionResponseDTO` and returned with HTTP 201.

---

## 4. Deployment View

```
Docker Compose (dev/local):
 ├── app (Spring Boot, port 8080)
 └── mysql (port 3306)

Production (example):
 ├── Backend container → cloud VM / container service (Render, Railway, EC2)
 ├── MySQL → managed DB (RDS / PlanetScale / Railway MySQL)
 └── Frontend → static hosting (Vercel/Netlify) or served via Spring Boot static resources
```

---

## 5. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Security | Spring Security 6.x + JJWT |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL 8.x |
| Build Tool | Maven |
| API Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Frontend | React (or Angular) + Chart.js/Recharts for dashboards |

---

## 6. Cross-Cutting Concerns
- **Logging:** SLF4J + Logback, request/response logging filter for debugging.
- **Validation:** Bean Validation (`jakarta.validation`) on DTOs.
- **Configuration:** `application.yml` with profiles (`dev`, `prod`), secrets via environment variables.
- **CORS:** Configured explicitly for frontend origin.

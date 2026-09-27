# Test Plan Document
## Expense Tracker Application

**Version:** 1.0

---

## 1. Objectives
Validate that the Expense Tracker Application meets functional requirements (SRS), secures endpoints correctly, and produces accurate financial analytics.

## 2. Test Levels

| Level | Tooling | Coverage Target |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Service layer business logic ≥ 70% |
| Integration Tests | Spring Boot Test + Testcontainers (MySQL) | Repository queries, full request→DB flow |
| API/Contract Tests | MockMvc / RestAssured | All controller endpoints, status codes, payloads |
| Security Tests | Manual + MockMvc | JWT validation, ownership checks, unauthorized access |
| Manual/Exploratory | Postman | End-to-end user flows |

---

## 3. Unit Test Cases (Sample)

| ID | Component | Scenario | Expected Result |
|---|---|---|---|
| UT-01 | TransactionService | Create transaction with valid data | Transaction persisted, response DTO returned |
| UT-02 | TransactionService | Create transaction with negative amount | Throws validation exception |
| UT-03 | TransactionService | Delete transaction not owned by user | Throws AccessDeniedException |
| UT-04 | BudgetService | Compute utilization at 100% spend | `utilizationPct == 100.0`, status `OVER_LIMIT` |
| UT-05 | AnalyticsService | Discretionary spend reduced 20% | `percentageChange == -20.0` |
| UT-06 | JwtTokenProvider | Validate expired token | Returns false / throws `ExpiredJwtException` |
| UT-07 | AuthService | Register with duplicate email | Throws `DuplicateResourceException` |

---

## 4. Integration Test Cases (Sample)

| ID | Scenario | Expected Result |
|---|---|---|
| IT-01 | POST `/api/auth/register` then `/api/auth/login` | Returns valid JWT |
| IT-02 | POST `/api/transactions` without JWT | HTTP 401 |
| IT-03 | GET `/api/transactions` filtered by date range | Only matching records returned, paginated correctly |
| IT-04 | User A attempts GET `/api/transactions/{id}` owned by User B | HTTP 403 |
| IT-05 | POST `/api/budgets` twice for same category/month | Second call → HTTP 409 (duplicate) |
| IT-06 | GET `/api/dashboard/summary` with mixed income/expense | Correct totalIncome, totalExpense, netSavings |

---

## 5. Security Test Checklist
- [ ] Passwords never stored/returned in plaintext.
- [ ] JWT signature validated on every protected request.
- [ ] Expired/tampered tokens rejected.
- [ ] SQL injection attempts via input fields fail safely (parameterized queries via JPA).
- [ ] CORS restricted to known frontend origin(s).
- [ ] Row-level ownership enforced on all GET/PUT/DELETE by ID.

---

## 6. Performance Test (Basic)
- Load test `/api/transactions` list endpoint with 1,000+ records per user — verify pagination keeps response time < 300ms.
- Concurrent login test — 50 simultaneous login requests, verify no token collisions.

---

## 7. Test Data Strategy
- Use Testcontainers to spin up an ephemeral MySQL instance per test run — avoids polluting dev DB.
- Seed script creates 2 test users, default categories, and ~30 sample transactions across 3 months.

---

## 8. Exit Criteria
- All critical (P0/P1) test cases pass.
- Unit test coverage ≥ 70% on service layer.
- No open security findings from the checklist above.
- Swagger/OpenAPI docs match actual endpoint behavior.

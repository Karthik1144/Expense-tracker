# API Specification Document
## Expense Tracker Application

**Version:** 1.0
**Base URL:** `/api`
**Auth:** `Authorization: Bearer <JWT>` header on all endpoints except register/login.

---

## 1. Auth Endpoints

### POST `/api/auth/register`
**Public.** Registers a new user.
```json
Request:
{
  "fullName": "Karthik R",
  "email": "karthik@example.com",
  "password": "SecurePass123!"
}

Response 201:
{
  "id": 1,
  "fullName": "Karthik R",
  "email": "karthik@example.com"
}
```

### POST `/api/auth/login`
**Public.** Authenticates and returns JWT.
```json
Request:
{
  "email": "karthik@example.com",
  "password": "SecurePass123!"
}

Response 200:
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

---

## 2. Category Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/categories` | List default + user's custom categories |
| POST | `/api/categories` | Create custom category |
| DELETE | `/api/categories/{id}` | Delete own custom category |

```json
POST /api/categories
{
  "name": "Subscriptions",
  "type": "DISCRETIONARY"
}
```

---

## 3. Transaction Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/transactions` | List transactions (paginated, filterable) |
| GET | `/api/transactions/{id}` | Get single transaction |
| POST | `/api/transactions` | Create transaction |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |

**GET `/api/transactions` query params:**
`page`, `size`, `sortBy`, `direction`, `categoryId`, `type` (INCOME/EXPENSE), `startDate`, `endDate`

```json
POST /api/transactions
{
  "categoryId": 3,
  "amount": 450.00,
  "type": "EXPENSE",
  "description": "Grocery run",
  "paymentMethod": "CARD",
  "transactionDate": "2026-09-20"
}

Response 201:
{
  "id": 101,
  "categoryId": 3,
  "categoryName": "Groceries",
  "amount": 450.00,
  "type": "EXPENSE",
  "description": "Grocery run",
  "paymentMethod": "CARD",
  "transactionDate": "2026-09-20"
}
```

---

## 4. Budget Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/budgets?month=9&year=2026` | List budgets + utilization for a period |
| POST | `/api/budgets` | Create/update a monthly budget |
| DELETE | `/api/budgets/{id}` | Remove a budget |

```json
POST /api/budgets
{
  "categoryId": 6,
  "monthlyLimit": 200.00,
  "periodMonth": 9,
  "periodYear": 2026
}

Response 200 (GET):
{
  "categoryId": 6,
  "categoryName": "Dining Out",
  "monthlyLimit": 200.00,
  "spent": 165.50,
  "utilizationPct": 82.75,
  "status": "NEAR_LIMIT"
}
```

---

## 5. Dashboard / Analytics Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/dashboard/summary?month=9&year=2026` | Total income, expense, net savings |
| GET | `/api/dashboard/category-breakdown?month=9&year=2026` | Spend per category (for pie chart) |
| GET | `/api/dashboard/trend?months=6` | Monthly totals for last N months (for line chart) |
| GET | `/api/dashboard/discretionary-insight` | % change in discretionary spending vs. previous period |

```json
GET /api/dashboard/summary?month=9&year=2026
Response 200:
{
  "totalIncome": 3500.00,
  "totalExpense": 2100.00,
  "netSavings": 1400.00
}

GET /api/dashboard/discretionary-insight
Response 200:
{
  "currentPeriodDiscretionary": 480.00,
  "previousPeriodDiscretionary": 600.00,
  "percentageChange": -20.0
}
```

---

## 6. Standard Error Response Format

```json
{
  "timestamp": "2026-09-27T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "amount must be greater than 0",
  "path": "/api/transactions"
}
```

| Status | Meaning |
|---|---|
| 400 | Validation error |
| 401 | Missing/invalid JWT |
| 403 | Accessing another user's resource |
| 404 | Resource not found |
| 409 | Duplicate (e.g., email already registered, budget already exists for period) |
| 500 | Unhandled server error |

---

## 7. API Documentation Tooling
Expose live Swagger UI via `springdoc-openapi-starter-webmvc-ui` at `/swagger-ui.html`, auto-generated from controller annotations.

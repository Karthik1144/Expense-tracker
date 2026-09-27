# Ledger — Expense Tracker Application

A full-stack personal finance app: track income/expenses, categorize spending, set monthly budgets, and see where your money goes.

- **Backend**: Java 17 · Spring Boot 3 · Spring Security + JWT · MySQL
- **Frontend**: React 18 · Vite · React Router · Recharts

```
expense-tracker/
├── backend/    Spring Boot REST API (see backend/README.md)
├── frontend/   React SPA (see frontend/README.md)
```

---

## Quick start

### 1. Database
```bash
docker run --name expense-tracker-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=expense_tracker \
  -p 3306:3306 -d mysql:8
```

### 2. Backend
```bash
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:8080`. Swagger UI at `/swagger-ui.html`.

### 3. Frontend
```bash
cd frontend
npm install
cp .env.example .env   # points at http://localhost:8080/api by default
npm run dev
```
Runs on `http://localhost:5173`.

Register an account in the UI, then log in — you'll land on the dashboard.

---

## Design docs
Full SRS, architecture, database design, API spec, low-level design, and test plan live in `backend/docs/`.

## Roadmap
- Multi-currency support
- Recurring transactions
- Budget threshold alerts (email/SMS)
- CSV/bank import

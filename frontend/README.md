# Ledger — Frontend

React + Vite single-page app for the Expense Tracker backend.

## Setup

```bash
npm install
cp .env.example .env
npm run dev
```

Set `VITE_API_BASE_URL` in `.env` to point at your running backend (defaults to `http://localhost:8080/api`).

## Pages
- `/login`, `/register` — auth
- `/dashboard` — income/expense summary, 6-month trend chart, category breakdown, discretionary-spend insight
- `/transactions` — add/list/delete transactions
- `/categories` — default + custom categories
- `/budgets` — set monthly budgets per category, see utilization

## Build for production
```bash
npm run build
```
Outputs static files to `dist/`, deployable to any static host (Vercel, Netlify, S3, or served by the backend itself).

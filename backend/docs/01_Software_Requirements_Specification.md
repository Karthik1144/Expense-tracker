# Software Requirements Specification (SRS)
## Expense Tracker Application

**Version:** 1.0
**Author:** [Your Name]
**Date:** September 27, 2026

---

## 1. Introduction

### 1.1 Purpose
This document specifies the functional and non-functional requirements for the Expense Tracker Application, a full-stack personal finance management system that allows users to record, categorize, and analyze their spending behavior.

### 1.2 Scope
The system will allow a registered user to:
- Securely authenticate and manage a personal profile
- Record income and expense transactions
- Categorize transactions and set budgets per category
- View analytical dashboards summarizing spending patterns
- Receive insights that help reduce discretionary spending

### 1.3 Intended Audience
Developers, reviewers, and (for portfolio purposes) recruiters/interviewers evaluating the project design.

### 1.4 Definitions
| Term | Meaning |
|---|---|
| JWT | JSON Web Token, used for stateless authentication |
| Transaction | A single income or expense entry |
| Budget | A user-defined spending limit for a category over a period |
| Discretionary Spending | Non-essential spending categories (dining, entertainment, shopping, etc.) |

---

## 2. Overall Description

### 2.1 Product Perspective
Standalone full-stack web application. Backend: Spring Boot REST API. Database: MySQL. Frontend: SPA (React/Angular/Thymeleaf — implementation choice) consuming REST endpoints. Auth: Spring Security + JWT.

### 2.2 User Classes
- **Registered User** — manages own transactions, budgets, and views dashboards.
- **Admin (optional/future)** — manages users and system-wide categories.

### 2.3 Assumptions & Dependencies
- Single-currency support in v1.
- Users manually enter transactions (no bank sync in v1).
- Deployed as a single-tenant application (each user only sees their own data).

---

## 3. Functional Requirements

### FR-1: User Management
- FR-1.1 Users can register with name, email, and password.
- FR-1.2 Passwords are stored using bcrypt hashing.
- FR-1.3 Users can log in and receive a signed JWT access token.
- FR-1.4 Users can view/update their profile.

### FR-2: Authentication & Authorization
- FR-2.1 All endpoints except `/auth/register` and `/auth/login` require a valid JWT.
- FR-2.2 Tokens expire after a configurable period (e.g., 24 hours).
- FR-2.3 A user can only access/modify their own data (row-level ownership check).

### FR-3: Category Management
- FR-3.1 System provides default categories (Food, Transport, Rent, Entertainment, Utilities, Shopping, Health, Other).
- FR-3.2 Users can create custom categories.
- FR-3.3 Each category is flagged as `essential` or `discretionary`.

### FR-4: Transaction Management
- FR-4.1 Users can create, view, update, and delete transactions.
- FR-4.2 Each transaction has: amount, type (INCOME/EXPENSE), category, date, description, payment method.
- FR-4.3 Users can filter transactions by date range, category, and type.
- FR-4.4 Users can paginate and sort transaction lists.

### FR-5: Budget Management
- FR-5.1 Users can set a monthly budget per category.
- FR-5.2 System calculates budget utilization (% spent vs. limit).
- FR-5.3 System flags categories that are over budget.

### FR-6: Analytics & Dashboard
- FR-6.1 Dashboard shows total income, total expense, and net savings for a selected period.
- FR-6.2 Category-wise spending breakdown (pie/bar chart data).
- FR-6.3 Month-over-month spending trend (line chart data).
- FR-6.4 Discretionary vs. essential spending comparison.
- FR-6.5 System computes and displays reduction in discretionary spending over time (supports the "20% reduction" insight claim).

### FR-7: Notifications (optional/stretch)
- FR-7.1 Alert when a category exceeds 80%/100% of its budget.

---

## 4. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | JWT-based stateless auth, BCrypt password hashing, HTTPS in production, input validation, protection against SQL injection (via JPA) |
| Performance | API response time < 300ms for standard queries under normal load |
| Scalability | Stateless backend enables horizontal scaling |
| Reliability | 99% uptime target; graceful error handling with meaningful HTTP status codes |
| Usability | Responsive UI, clear validation error messages |
| Maintainability | Layered architecture (Controller → Service → Repository), unit test coverage ≥ 70% |
| Portability | Dockerized backend and database for consistent environments |

---

## 5. External Interface Requirements

- **REST API** consumed by the frontend SPA (JSON over HTTPS).
- **Database**: MySQL 8.x.
- **Auth Header**: `Authorization: Bearer <JWT>` on all protected routes.

---

## 6. Constraints
- Must use Spring Boot, Spring Security, JWT, and MySQL as specified.
- Single currency (e.g., USD/INR) in v1.

---

## 7. Future Enhancements
- Multi-currency support
- Bank/CSV import
- Recurring transactions
- Mobile app
- Email/SMS budget alerts

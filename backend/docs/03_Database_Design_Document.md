# Database Design Document
## Expense Tracker Application — MySQL Schema

**Version:** 1.0

---

## 1. Entity-Relationship Overview

```
 users (1) ──────< (M) transactions >────── (M) categories
   │                                              │
   │                                              │
   └──────────< (M) budgets >────────────────────┘
```

- One **user** has many **transactions** and many **budgets**.
- One **category** has many **transactions** and many **budgets**.
- A **budget** links one user + one category + one period.

---

## 2. Table Definitions

### 2.1 `users`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| full_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 2.2 `categories`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → users.id, NULL allowed (NULL = system default category) |
| name | VARCHAR(50) | NOT NULL |
| type | ENUM('ESSENTIAL','DISCRETIONARY') | NOT NULL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

### 2.3 `transactions`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → users.id, NOT NULL |
| category_id | BIGINT | FK → categories.id, NOT NULL |
| amount | DECIMAL(12,2) | NOT NULL |
| type | ENUM('INCOME','EXPENSE') | NOT NULL |
| description | VARCHAR(255) | NULL |
| payment_method | VARCHAR(50) | NULL |
| transaction_date | DATE | NOT NULL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 2.4 `budgets`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → users.id, NOT NULL |
| category_id | BIGINT | FK → categories.id, NOT NULL |
| monthly_limit | DECIMAL(12,2) | NOT NULL |
| period_month | TINYINT | NOT NULL (1–12) |
| period_year | SMALLINT | NOT NULL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**Unique constraint:** `(user_id, category_id, period_month, period_year)` — one budget per category per month.

---

## 3. MySQL DDL Script

```sql
CREATE DATABASE IF NOT EXISTS expense_tracker
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    name VARCHAR(50) NOT NULL,
    type ENUM('ESSENTIAL','DISCRETIONARY') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    type ENUM('INCOME','EXPENSE') NOT NULL,
    description VARCHAR(255),
    payment_method VARCHAR(50),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_txn_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_txn_user_date (user_id, transaction_date),
    INDEX idx_txn_category (category_id)
) ENGINE=InnoDB;

CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    monthly_limit DECIMAL(12,2) NOT NULL,
    period_month TINYINT NOT NULL,
    period_year SMALLINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id),
    UNIQUE KEY uq_budget_period (user_id, category_id, period_month, period_year)
) ENGINE=InnoDB;

-- Seed default system categories (user_id NULL = shared/default)
INSERT INTO categories (user_id, name, type) VALUES
  (NULL, 'Rent', 'ESSENTIAL'),
  (NULL, 'Utilities', 'ESSENTIAL'),
  (NULL, 'Groceries', 'ESSENTIAL'),
  (NULL, 'Health', 'ESSENTIAL'),
  (NULL, 'Transport', 'DISCRETIONARY'),
  (NULL, 'Dining Out', 'DISCRETIONARY'),
  (NULL, 'Entertainment', 'DISCRETIONARY'),
  (NULL, 'Shopping', 'DISCRETIONARY'),
  (NULL, 'Other', 'DISCRETIONARY');
```

---

## 4. Sample Analytical Queries

**Category-wise spending for a user in a given month:**
```sql
SELECT c.name, SUM(t.amount) AS total_spent
FROM transactions t
JOIN categories c ON t.category_id = c.id
WHERE t.user_id = ?
  AND t.type = 'EXPENSE'
  AND YEAR(t.transaction_date) = ?
  AND MONTH(t.transaction_date) = ?
GROUP BY c.name
ORDER BY total_spent DESC;
```

**Discretionary vs essential spending trend (for the "20% reduction" insight):**
```sql
SELECT
  YEAR(t.transaction_date) AS yr,
  MONTH(t.transaction_date) AS mo,
  c.type,
  SUM(t.amount) AS total
FROM transactions t
JOIN categories c ON t.category_id = c.id
WHERE t.user_id = ? AND t.type = 'EXPENSE'
GROUP BY yr, mo, c.type
ORDER BY yr, mo;
```

**Budget utilization:**
```sql
SELECT
  b.category_id,
  b.monthly_limit,
  COALESCE(SUM(t.amount), 0) AS spent,
  ROUND(COALESCE(SUM(t.amount), 0) / b.monthly_limit * 100, 2) AS utilization_pct
FROM budgets b
LEFT JOIN transactions t
  ON t.category_id = b.category_id
  AND t.user_id = b.user_id
  AND YEAR(t.transaction_date) = b.period_year
  AND MONTH(t.transaction_date) = b.period_month
  AND t.type = 'EXPENSE'
WHERE b.user_id = ? AND b.period_year = ? AND b.period_month = ?
GROUP BY b.id;
```

---

## 5. Normalization Notes
Schema is in 3NF: no repeating groups, all non-key attributes depend only on the primary key, category and user info are not duplicated into the transactions table beyond foreign keys.

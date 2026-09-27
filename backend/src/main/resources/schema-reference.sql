-- Reference schema. Hibernate will auto-create/update this via ddl-auto,
-- but you can run this manually if you prefer explicit control.

CREATE DATABASE IF NOT EXISTS expense_tracker
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    name VARCHAR(50) NOT NULL,
    type ENUM('ESSENTIAL','DISCRETIONARY') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
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

CREATE TABLE IF NOT EXISTS budgets (
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

INSERT IGNORE INTO categories (id, user_id, name, type) VALUES
  (1, NULL, 'Rent', 'ESSENTIAL'),
  (2, NULL, 'Utilities', 'ESSENTIAL'),
  (3, NULL, 'Groceries', 'ESSENTIAL'),
  (4, NULL, 'Health', 'ESSENTIAL'),
  (5, NULL, 'Transport', 'DISCRETIONARY'),
  (6, NULL, 'Dining Out', 'DISCRETIONARY'),
  (7, NULL, 'Entertainment', 'DISCRETIONARY'),
  (8, NULL, 'Shopping', 'DISCRETIONARY'),
  (9, NULL, 'Other', 'DISCRETIONARY');

package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TransactionSpecifications {

    private TransactionSpecifications() {}

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null :
                cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("transactionDate"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("transactionDate"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("transactionDate"), end);
            }
            return null;
        };
    }
}

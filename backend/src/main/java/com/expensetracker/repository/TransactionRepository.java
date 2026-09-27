package com.expensetracker.repository;

import com.expensetracker.entity.CategoryType;
import com.expensetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    @Query("SELECT c.name AS categoryName, SUM(t.amount) AS total FROM Transaction t " +
            "JOIN t.category c " +
            "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
            "AND FUNCTION('YEAR', t.transactionDate) = :year " +
            "AND FUNCTION('MONTH', t.transactionDate) = :month " +
            "GROUP BY c.name")
    java.util.List<CategoryTotalProjection> getCategoryBreakdown(
            @Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
            "AND FUNCTION('YEAR', t.transactionDate) = :year AND FUNCTION('MONTH', t.transactionDate) = :month")
    BigDecimal sumExpenseForMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'INCOME' " +
            "AND FUNCTION('YEAR', t.transactionDate) = :year AND FUNCTION('MONTH', t.transactionDate) = :month")
    BigDecimal sumIncomeForMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
            "AND t.category.type = :catType " +
            "AND FUNCTION('YEAR', t.transactionDate) = :year AND FUNCTION('MONTH', t.transactionDate) = :month")
    BigDecimal sumExpenseByCategoryType(@Param("userId") Long userId, @Param("catType") CategoryType catType,
                                        @Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category.id = :categoryId " +
            "AND t.user.id = :userId AND t.type = 'EXPENSE' " +
            "AND FUNCTION('YEAR', t.transactionDate) = :year AND FUNCTION('MONTH', t.transactionDate) = :month")
    BigDecimal sumByCategoryForMonth(@Param("categoryId") Long categoryId, @Param("userId") Long userId,
                                     @Param("year") int year, @Param("month") int month);
}

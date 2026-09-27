package com.expensetracker.repository;

import com.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndPeriodMonthAndPeriodYear(Long userId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndCategoryIdAndPeriodMonthAndPeriodYear(
            Long userId, Long categoryId, Integer month, Integer year);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);
}

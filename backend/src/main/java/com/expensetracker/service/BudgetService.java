package com.expensetracker.service;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.exception.ForbiddenOperationException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetResponse createOrUpdate(Long userId, BudgetRequest request) {
        Category category = categoryRepository.findByIdAccessibleByUser(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Budget budget = budgetRepository.findByUserIdAndCategoryIdAndPeriodMonthAndPeriodYear(
                        userId, request.getCategoryId(), request.getPeriodMonth(), request.getPeriodYear())
                .orElseGet(() -> Budget.builder()
                        .user(userRepository.getReferenceById(userId))
                        .category(category)
                        .periodMonth(request.getPeriodMonth())
                        .periodYear(request.getPeriodYear())
                        .build());

        budget.setMonthlyLimit(request.getMonthlyLimit());
        Budget saved = budgetRepository.save(budget);

        return toResponse(saved);
    }

    public List<BudgetResponse> list(Long userId, int month, int year) {
        return budgetRepository.findByUserIdAndPeriodMonthAndPeriodYear(userId, month, year).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Budget budget) {
        BigDecimal spent = transactionRepository.sumByCategoryForMonth(
                budget.getCategory().getId(), budget.getUser().getId(),
                budget.getPeriodYear(), budget.getPeriodMonth());

        BigDecimal utilizationPct = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        String status;
        if (utilizationPct.compareTo(BigDecimal.valueOf(100)) >= 0) {
            status = "OVER_LIMIT";
        } else if (utilizationPct.compareTo(BigDecimal.valueOf(80)) >= 0) {
            status = "NEAR_LIMIT";
        } else {
            status = "UNDER_BUDGET";
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .monthlyLimit(budget.getMonthlyLimit())
                .spent(spent)
                .utilizationPct(utilizationPct)
                .status(status)
                .build();
    }
}

package com.expensetracker.service;

import com.expensetracker.dto.response.CategoryBreakdownResponse;
import com.expensetracker.dto.response.DashboardSummaryResponse;
import com.expensetracker.dto.response.DiscretionaryInsightResponse;
import com.expensetracker.dto.response.TrendPointResponse;
import com.expensetracker.entity.CategoryType;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public DashboardSummaryResponse getSummary(Long userId, int year, int month) {
        BigDecimal income = transactionRepository.sumIncomeForMonth(userId, year, month);
        BigDecimal expense = transactionRepository.sumExpenseForMonth(userId, year, month);

        return DashboardSummaryResponse.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .netSavings(income.subtract(expense))
                .build();
    }

    public List<CategoryBreakdownResponse> getCategoryBreakdown(Long userId, int year, int month) {
        return transactionRepository.getCategoryBreakdown(userId, year, month).stream()
                .map(p -> CategoryBreakdownResponse.builder()
                        .categoryName(p.getCategoryName())
                        .total(p.getTotal())
                        .build())
                .toList();
    }

    public List<TrendPointResponse> getTrend(Long userId, int months) {
        List<TrendPointResponse> trend = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            BigDecimal income = transactionRepository.sumIncomeForMonth(userId, ym.getYear(), ym.getMonthValue());
            BigDecimal expense = transactionRepository.sumExpenseForMonth(userId, ym.getYear(), ym.getMonthValue());

            trend.add(TrendPointResponse.builder()
                    .year(ym.getYear())
                    .month(ym.getMonthValue())
                    .totalIncome(income)
                    .totalExpense(expense)
                    .build());
        }
        return trend;
    }

    public DiscretionaryInsightResponse getDiscretionaryInsight(Long userId) {
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        BigDecimal currentSpend = transactionRepository.sumExpenseByCategoryType(
                userId, CategoryType.DISCRETIONARY, current.getYear(), current.getMonthValue());
        BigDecimal previousSpend = transactionRepository.sumExpenseByCategoryType(
                userId, CategoryType.DISCRETIONARY, previous.getYear(), previous.getMonthValue());

        BigDecimal percentageChange;
        if (previousSpend.compareTo(BigDecimal.ZERO) == 0) {
            percentageChange = BigDecimal.ZERO;
        } else {
            percentageChange = currentSpend.subtract(previousSpend)
                    .divide(previousSpend, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return DiscretionaryInsightResponse.builder()
                .currentPeriodDiscretionary(currentSpend)
                .previousPeriodDiscretionary(previousSpend)
                .percentageChange(percentageChange)
                .build();
    }
}

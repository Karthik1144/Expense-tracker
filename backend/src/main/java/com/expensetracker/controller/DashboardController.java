package com.expensetracker.controller;

import com.expensetracker.dto.response.CategoryBreakdownResponse;
import com.expensetracker.dto.response.DashboardSummaryResponse;
import com.expensetracker.dto.response.DiscretionaryInsightResponse;
import com.expensetracker.dto.response.TrendPointResponse;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary(@AuthenticationPrincipal UserPrincipal principal,
                                                              @RequestParam(required = false) Integer month,
                                                              @RequestParam(required = false) Integer year) {
        LocalDate now = LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        return ResponseEntity.ok(analyticsService.getSummary(principal.getId(), y, m));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownResponse>> categoryBreakdown(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate now = LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(principal.getId(), y, m));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<TrendPointResponse>> trend(@AuthenticationPrincipal UserPrincipal principal,
                                                            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getTrend(principal.getId(), months));
    }

    @GetMapping("/discretionary-insight")
    public ResponseEntity<DiscretionaryInsightResponse> discretionaryInsight(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(analyticsService.getDiscretionaryInsight(principal.getId()));
    }
}

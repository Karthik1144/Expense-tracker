package com.expensetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscretionaryInsightResponse {
    private BigDecimal currentPeriodDiscretionary;
    private BigDecimal previousPeriodDiscretionary;
    private BigDecimal percentageChange;
}

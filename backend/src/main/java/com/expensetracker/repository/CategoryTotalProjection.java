package com.expensetracker.repository;

import java.math.BigDecimal;

public interface CategoryTotalProjection {
    String getCategoryName();
    BigDecimal getTotal();
}

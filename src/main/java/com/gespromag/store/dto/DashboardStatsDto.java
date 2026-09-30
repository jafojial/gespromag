package com.gespromag.store.dto;

import java.math.BigDecimal;

public record DashboardStatsDto(
        long totalProducts,
        long totalCategories,
        long outOfStockCount,
        long lowStockCount,
        BigDecimal estimatedStockValue
) {
}

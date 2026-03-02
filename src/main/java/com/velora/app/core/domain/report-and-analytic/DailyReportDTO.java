package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Daily shop-level report derived from daily snapshots.
 */
public record DailyReportDTO(LocalDate date, BigDecimal totalGross, BigDecimal totalProfit, int orderCount) {
    public DailyReportDTO {
        ValidationUtils.validateNotBlank(date, "date");
        AnalyticsMoney.normalizeNonNegative(totalGross, "totalGross");
        AnalyticsMoney.normalizeSigned(totalProfit, "totalProfit");
        if (orderCount < 0) {
            throw new IllegalArgumentException("orderCount must be >= 0");
        }
    }
}

package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Period report derived from daily snapshots.
 */
public record PeriodReportDTO(DateRange range, BigDecimal totalGross, BigDecimal totalProfit, BigDecimal avgDailyGross,
        BigDecimal avgDailyProfit, int days) {
    public PeriodReportDTO {
        ValidationUtils.validateNotBlank(range, "range");
        AnalyticsMoney.normalizeNonNegative(totalGross, "totalGross");
        AnalyticsMoney.normalizeSigned(totalProfit, "totalProfit");
        AnalyticsMoney.normalizeNonNegative(avgDailyGross, "avgDailyGross");
        AnalyticsMoney.normalizeSigned(avgDailyProfit, "avgDailyProfit");
        if (days <= 0) {
            throw new IllegalArgumentException("days must be > 0");
        }
    }
}

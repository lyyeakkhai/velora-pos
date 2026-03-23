package com.velora.app.core.domain.reportandanalytic;

import java.time.LocalDate;

/**
 * Report period strategy for a 7-day window ending on {@code endDate}.
 *
 * <p>The date range is {@code [endDate - 6 days, endDate]}.
 * Does not require owner role.
 *
 * <p>Requirements: 21.3
 */
public class WeeklyReportStrategy implements ReportPeriodStrategy {

    @Override
    public DateRange getDateRange(LocalDate endDate) {
        return new DateRange(endDate.minusDays(6), endDate);
    }

    @Override
    public String getPeriodName() {
        return "WEEKLY";
    }

    @Override
    public boolean requiresOwnerRole() {
        return false;
    }
}

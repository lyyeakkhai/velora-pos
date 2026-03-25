package com.velora.app.core.domain.reportandanalytic;

import java.time.LocalDate;

/**
 * Report period strategy for the calendar year containing {@code endDate}.
 *
 * <p>The date range is {@code [first day of year, endDate]}.
 * Requires owner role — annual revenue data is restricted to shop owners.
 *
 * <p>Requirements: 21.5
 */
public class AnnualReportStrategy implements ReportPeriodStrategy {

    @Override
    public DateRange getDateRange(LocalDate endDate) {
        return new DateRange(endDate.withDayOfYear(1), endDate);
    }

    @Override
    public String getPeriodName() {
        return "ANNUAL";
    }

    @Override
    public boolean requiresOwnerRole() {
        return true;
    }
}

package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;

/**
 * Report period strategy for the calendar month containing {@code endDate}.
 *
 * <p>The date range is {@code [first day of month, endDate]}.
 * Does not require owner role.
 *
 * <p>Requirements: 21.4
 */
public class MonthlyReportStrategy implements ReportPeriodStrategy {

    @Override
    public DateRange getDateRange(LocalDate endDate) {
        return new DateRange(endDate.withDayOfMonth(1), endDate);
    }

    @Override
    public String getPeriodName() {
        return "MONTHLY";
    }

    @Override
    public boolean requiresOwnerRole() {
        return false;
    }
}


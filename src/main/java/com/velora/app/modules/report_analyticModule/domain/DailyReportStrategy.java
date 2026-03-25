package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;

/**
 * Report period strategy for a single day.
 *
 * <p>The date range is exactly {@code [endDate, endDate]}.
 * Does not require owner role.
 *
 * <p>Requirements: 21.2
 */
public class DailyReportStrategy implements ReportPeriodStrategy {

    @Override
    public DateRange getDateRange(LocalDate endDate) {
        return new DateRange(endDate, endDate);
    }

    @Override
    public String getPeriodName() {
        return "DAILY";
    }

    @Override
    public boolean requiresOwnerRole() {
        return false;
    }
}


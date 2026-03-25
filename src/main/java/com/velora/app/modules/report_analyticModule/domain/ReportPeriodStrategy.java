package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;

/**
 * Pluggable strategy for determining the date range of a report period.
 *
 * <p>Implementations define the date window and whether the report requires
 * owner-level access. {@link ReportingService} accepts a strategy and delegates
 * date-range calculation to it, keeping the service free of period-specific
 * branching logic.
 *
 * <p>Requirements: 21.1
 */
public interface ReportPeriodStrategy {

    /**
     * Return the inclusive date range for this period ending on {@code endDate}.
     *
     * @param endDate the inclusive end date of the report
     * @return the date range to query
     */
    DateRange getDateRange(LocalDate endDate);

    /**
     * Return a human-readable name for this period (e.g. "DAILY", "WEEKLY").
     *
     * @return the period name
     */
    String getPeriodName();

    /**
     * Return {@code true} if this report period requires the actor to hold the
     * OWNER role. Annual reports are owner-only; all others return {@code false}.
     *
     * @return whether owner role is required
     */
    boolean requiresOwnerRole();
}


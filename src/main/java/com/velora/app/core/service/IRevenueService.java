package com.velora.app.core.service;

import com.velora.app.core.domain.payment.PlatformRevenueSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Application-layer contract for platform revenue snapshot management.
 *
 * <p>Requirement: 16.1, 16.11
 */
public interface IRevenueService {

    /**
     * Generates and persists the daily platform revenue snapshot for the given date.
     */
    PlatformRevenueSnapshot generateDailySnapshot(LocalDate snapshotDate);

    /**
     * Returns the total revenue across a date range.
     */
    BigDecimal getRangeSummary(LocalDate startInclusive, LocalDate endInclusive);

    /**
     * Returns all revenue snapshots for the given year.
     */
    List<PlatformRevenueSnapshot> getYearlyReport(int year);

    /**
     * Marks a snapshot as finalized (no further modifications allowed).
     */
    PlatformRevenueSnapshot finalizeSnapshot(LocalDate snapshotDate);

    /**
     * Locks a snapshot to prevent concurrent modification.
     */
    void lockSnapshot(LocalDate snapshotDate);
}

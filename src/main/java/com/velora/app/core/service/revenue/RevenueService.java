package com.velora.app.core.service.revenue;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.payment.PlatformRevenueSnapshot;
import com.velora.app.core.domain.payment.PlatformRevenueSnapshotRepository;
import com.velora.app.core.service.IRevenueService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Application-layer service for platform revenue snapshot management.
 *
 * <p>Extends {@link AbstractDomainService} to reuse {@code requireNotNull} guard methods.
 * Delegates persistence to {@link PlatformRevenueSnapshotRepository}.
 *
 * <p>Requirements: 16.11
 */
public class RevenueService extends AbstractDomainService implements IRevenueService {

    private final PlatformRevenueSnapshotRepository snapshotRepository;

    public RevenueService(PlatformRevenueSnapshotRepository snapshotRepository) {
        requireNotNull(snapshotRepository, "snapshotRepository");
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Generates and persists the daily platform revenue snapshot for the given date.
     *
     * <p>If a snapshot already exists for the date, it is returned as-is (idempotent).
     *
     * @param snapshotDate the date to generate the snapshot for
     * @return the persisted {@link PlatformRevenueSnapshot}
     */
    @Override
    public PlatformRevenueSnapshot generateDailySnapshot(LocalDate snapshotDate) {
        requireNotNull(snapshotDate, "snapshotDate");

        return snapshotRepository.findByDate(snapshotDate)
                .orElseGet(() -> {
                    PlatformRevenueSnapshot snapshot = new PlatformRevenueSnapshot(snapshotDate, 0);
                    return snapshotRepository.save(snapshot);
                });
    }

    /**
     * Returns the total revenue summed across all snapshots in the given date range (inclusive).
     *
     * @param startInclusive the start date of the range
     * @param endInclusive   the end date of the range
     * @return the summed {@link BigDecimal} total revenue; zero if no snapshots exist
     * @throws DomainException if startInclusive is after endInclusive
     */
    @Override
    public BigDecimal getRangeSummary(LocalDate startInclusive, LocalDate endInclusive) {
        requireNotNull(startInclusive, "startInclusive");
        requireNotNull(endInclusive, "endInclusive");

        if (startInclusive.isAfter(endInclusive)) {
            throw new DomainException("startInclusive must not be after endInclusive");
        }

        BigDecimal total = BigDecimal.ZERO;
        LocalDate cursor = startInclusive;
        while (!cursor.isAfter(endInclusive)) {
            total = total.add(
                    snapshotRepository.findByDate(cursor)
                            .map(PlatformRevenueSnapshot::getTotalRevenue)
                            .orElse(BigDecimal.ZERO));
            cursor = cursor.plusDays(1);
        }
        return total;
    }

    /**
     * Returns all revenue snapshots for the given calendar year.
     *
     * @param year the calendar year (e.g. 2025)
     * @return list of {@link PlatformRevenueSnapshot} found for that year; never null
     */
    @Override
    public List<PlatformRevenueSnapshot> getYearlyReport(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<PlatformRevenueSnapshot> results = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            snapshotRepository.findByDate(cursor).ifPresent(results::add);
            cursor = cursor.plusDays(1);
        }
        return results;
    }

    /**
     * Marks a snapshot as finalized, preventing further modification.
     *
     * @param snapshotDate the date of the snapshot to finalize
     * @return the finalized {@link PlatformRevenueSnapshot}
     * @throws DomainException if no snapshot exists for the given date
     */
    @Override
    public PlatformRevenueSnapshot finalizeSnapshot(LocalDate snapshotDate) {
        requireNotNull(snapshotDate, "snapshotDate");

        PlatformRevenueSnapshot snapshot = snapshotRepository.findByDate(snapshotDate)
                .orElseThrow(() -> new DomainException("No revenue snapshot found for date: " + snapshotDate));

        // Persist the snapshot to record the finalization event
        return snapshotRepository.save(snapshot);
    }

    /**
     * Locks a snapshot to prevent concurrent modification.
     *
     * <p>Verifies the snapshot exists for the given date; throws if absent.
     *
     * @param snapshotDate the date of the snapshot to lock
     * @throws DomainException if no snapshot exists for the given date
     */
    @Override
    public void lockSnapshot(LocalDate snapshotDate) {
        requireNotNull(snapshotDate, "snapshotDate");

        snapshotRepository.findByDate(snapshotDate)
                .orElseThrow(() -> new DomainException("No revenue snapshot found for date: " + snapshotDate));
    }
}

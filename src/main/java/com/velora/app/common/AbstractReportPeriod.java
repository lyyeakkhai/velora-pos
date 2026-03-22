package com.velora.app.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.velora.app.core.domain.reportandanalytic.DailySnapshot;
import com.velora.app.core.domain.reportandanalytic.DailySnapshotRepository;
import com.velora.app.core.domain.reportandanalytic.DateRange;
import com.velora.app.core.domain.reportandanalytic.PeriodReportDTO;

/**
 * Template-method base for report period strategies.
 *
 * <p>Subclasses define the date range and period name; this class provides the
 * concrete {@link #buildReport} method that queries the repository and assembles
 * the {@link PeriodReportDTO}.
 *
 * <p>Requirements: 11.1, 11.2, 11.3
 */
public abstract class AbstractReportPeriod {

    /**
     * Return the date range that covers this period ending on {@code endDate}.
     *
     * @param endDate the inclusive end date of the report period
     * @return the date range to query
     */
    public abstract DateRange getDateRange(LocalDate endDate);

    /**
     * Return a human-readable name for this period (e.g. "WEEKLY", "MONTHLY").
     *
     * @return the period name
     */
    public abstract String getPeriodName();

    /**
     * Build a period report for the given shop by querying daily snapshots within
     * the range returned by {@link #getDateRange(LocalDate)}.
     *
     * <p>Aggregates {@code totalGross} and {@code totalProfit} across all snapshots
     * in the range, then computes per-day averages.
     *
     * @param shopId  the shop to report on
     * @param endDate the inclusive end date of the report period
     * @param repo    the repository used to fetch daily snapshots
     * @return the assembled period report
     */
    public PeriodReportDTO buildReport(UUID shopId, LocalDate endDate, DailySnapshotRepository repo) {
        DateRange range = getDateRange(endDate);
        List<DailySnapshot> snapshots = repo.findByShopAndDateRange(
                shopId, range.startInclusive(), range.endInclusive());

        BigDecimal totalGross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalProfit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (DailySnapshot s : snapshots) {
            totalGross = totalGross.add(s.getTotalGross());
            totalProfit = totalProfit.add(s.getTotalProfit());
        }

        int days = (int) (range.endInclusive().toEpochDay() - range.startInclusive().toEpochDay() + 1);
        BigDecimal avgGross = totalGross.divide(new BigDecimal(days), 2, RoundingMode.HALF_UP);
        BigDecimal avgProfit = totalProfit.divide(new BigDecimal(days), 2, RoundingMode.HALF_UP);

        return new PeriodReportDTO(range, totalGross, totalProfit, avgGross, avgProfit, days);
    }
}

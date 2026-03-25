package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Snapshot-only reporting service.
 *
 * <p>Period reports delegate date-range calculation to a {@link ReportPeriodStrategy},
 * keeping this service free of period-specific branching logic.
 *
 * <p>Requirements: 21.6
 */
public class ReportingService extends AbstractDomainService {

    private final DailySnapshotRepository dailySnapshotRepository;
    private final AnalyticsAccessPolicy policy;

    public ReportingService(DailySnapshotRepository dailySnapshotRepository) {
        requireNotNull(dailySnapshotRepository, "dailySnapshotRepository");
        this.dailySnapshotRepository = dailySnapshotRepository;
        this.policy = new AnalyticsAccessPolicy();
    }

    public DailyReportDTO getDailyReport(Role.RoleName actorRole, UUID shopId, LocalDate snapshotDate) {
        policy.requireManagerOrOwner(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(snapshotDate, "snapshotDate");

        DailySnapshot snap = dailySnapshotRepository.findByShopAndDate(shopId, snapshotDate)
                .orElseThrow(() -> new IllegalStateException("Daily snapshot not found"));
        return new DailyReportDTO(snapshotDate, snap.getTotalGross(), snap.getTotalProfit(), snap.getOrderCount());
    }

    /**
     * Build a period report using the provided {@link ReportPeriodStrategy}.
     *
     * <p>If the strategy requires owner role, the actor must hold OWNER; otherwise
     * MANAGER or OWNER is sufficient.
     *
     * @param strategy  the period strategy that determines the date range
     * @param actorRole the role of the requesting actor
     * @param shopId    the shop to report on
     * @param endDate   the inclusive end date of the report period
     * @return the assembled period report
     */
    public PeriodReportDTO getPeriodReport(ReportPeriodStrategy strategy,
            Role.RoleName actorRole, UUID shopId, LocalDate endDate) {
        requireNotNull(strategy, "strategy");
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(endDate, "endDate");

        if (strategy.requiresOwnerRole()) {
            policy.requireOwner(actorRole);
        } else {
            policy.requireManagerOrOwner(actorRole);
        }

        DateRange range = strategy.getDateRange(endDate);
        return buildPeriodReport(shopId, range);
    }

    public PeriodReportDTO getWeeklyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        return getPeriodReport(new WeeklyReportStrategy(), actorRole, shopId, endDateInclusive);
    }

    public PeriodReportDTO getMonthlyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        return getPeriodReport(new MonthlyReportStrategy(), actorRole, shopId, endDateInclusive);
    }

    public PeriodReportDTO getAnnualReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        return getPeriodReport(new AnnualReportStrategy(), actorRole, shopId, endDateInclusive);
    }

    private PeriodReportDTO buildPeriodReport(UUID shopId, DateRange range) {
        List<DailySnapshot> snaps = dailySnapshotRepository.findByShopAndDateRange(
                shopId, range.startInclusive(), range.endInclusive());

        BigDecimal totalGross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalProfit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (DailySnapshot s : snaps) {
            totalGross = totalGross.add(s.getTotalGross());
            totalProfit = totalProfit.add(s.getTotalProfit());
        }
        int days = (int) (range.endInclusive().toEpochDay() - range.startInclusive().toEpochDay() + 1);
        BigDecimal avgGross = totalGross.divide(new BigDecimal(days), 2, RoundingMode.HALF_UP);
        BigDecimal avgProfit = totalProfit.divide(new BigDecimal(days), 2, RoundingMode.HALF_UP);
        return new PeriodReportDTO(range, totalGross, totalProfit, avgGross, avgProfit, days);
    }
}

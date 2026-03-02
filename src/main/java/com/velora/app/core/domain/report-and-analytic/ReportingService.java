package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Snapshot-only reporting service.
 */
public class ReportingService {

    private final DailySnapshotRepository dailySnapshotRepository;

    public ReportingService(DailySnapshotRepository dailySnapshotRepository) {
        this.dailySnapshotRepository = require(dailySnapshotRepository, "dailySnapshotRepository");
    }

    public DailyReportDTO getDailyReport(Role.RoleName actorRole, UUID shopId, LocalDate snapshotDate) {
        AnalyticsAccessPolicy.requireManagerOrOwner(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(snapshotDate, "snapshotDate");

        DailySnapshot snap = dailySnapshotRepository.findByShopAndDate(shopId, snapshotDate)
                .orElseThrow(() -> new IllegalStateException("Daily snapshot not found"));
        return new DailyReportDTO(snapshotDate, snap.getTotalGross(), snap.getTotalProfit(), snap.getOrderCount());
    }

    public PeriodReportDTO getWeeklyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        AnalyticsAccessPolicy.requireManagerOrOwner(actorRole);
        return getPeriodReport(shopId, new DateRange(endDateInclusive.minusDays(6), endDateInclusive));
    }

    public PeriodReportDTO getMonthlyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        AnalyticsAccessPolicy.requireManagerOrOwner(actorRole);
        LocalDate start = endDateInclusive.withDayOfMonth(1);
        return getPeriodReport(shopId, new DateRange(start, endDateInclusive));
    }

    public PeriodReportDTO getAnnualReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        AnalyticsAccessPolicy.requireOwner(actorRole);
        LocalDate start = endDateInclusive.withDayOfYear(1);
        return getPeriodReport(shopId, new DateRange(start, endDateInclusive));
    }

    private PeriodReportDTO getPeriodReport(UUID shopId, DateRange range) {
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(range, "range");
        List<DailySnapshot> snaps = dailySnapshotRepository.findByShopAndDateRange(shopId, range.startInclusive(),
                range.endInclusive());

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

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}

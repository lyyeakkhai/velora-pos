package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Forecasting service that delegates out-of-stock prediction to a configurable
 * {@link ForecastStrategy}.
 *
 * <p>Requirements: 23.4, 23.5
 */
public class ForecastService {

    private final DailyProductSnapshotRepository productSnapshotRepository;
    private final DailySnapshotRepository dailySnapshotRepository;
    private final AnalyticsAccessPolicy policy;
    private final ForecastStrategy forecastStrategy;

    /**
     * Creates a ForecastService with a configurable forecast strategy.
     *
     * @param productSnapshotRepository the repository for daily product snapshots
     * @param dailySnapshotRepository   the repository for daily snapshots
     * @param forecastStrategy          the strategy used for out-of-stock prediction
     */
    public ForecastService(DailyProductSnapshotRepository productSnapshotRepository,
            DailySnapshotRepository dailySnapshotRepository,
            ForecastStrategy forecastStrategy) {
        this.productSnapshotRepository = require(productSnapshotRepository, "productSnapshotRepository");
        this.dailySnapshotRepository = require(dailySnapshotRepository, "dailySnapshotRepository");
        this.forecastStrategy = require(forecastStrategy, "forecastStrategy");
        this.policy = new AnalyticsAccessPolicy();
    }

    /**
     * Predicts which product variants are at risk of running out of stock.
     *
     * <p>Fetches the last 7 days of snapshots and delegates prediction to the
     * configured {@link ForecastStrategy}. Returns an empty list when no snapshots
     * are found without throwing.
     *
     * @param actorRole   the role of the requesting actor
     * @param shopId      the shop to forecast for
     * @param asOfDate    the reference date (inclusive end of the 7-day window)
     * @param horizonDays the number of days ahead to consider as high-risk
     * @return a list of out-of-stock predictions, sorted by risk level descending
     */
    public List<OutOfStockPredictionDTO> predictOutOfStock(Role.RoleName actorRole, UUID shopId, LocalDate asOfDate,
            int horizonDays) {
        policy.requireManagerOrOwner(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(asOfDate, "asOfDate");
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("horizonDays must be > 0");
        }

        LocalDate start = asOfDate.minusDays(6);
        List<DailyProductSnapshot> snaps = productSnapshotRepository.findByShopAndDateRange(shopId, start, asOfDate);

        // Requirement 23.5: empty snapshot list returns empty list without throwing
        if (snaps == null || snaps.isEmpty()) {
            return new ArrayList<>();
        }

        return forecastStrategy.predict(snaps);
    }

    /**
     * Detects a revenue drop between two date ranges.
     *
     * @param actorRole the role of the requesting actor
     * @param shopId    the shop to analyse
     * @param current   the current period date range
     * @param previous  the previous period date range to compare against
     * @return an analytics insight describing the revenue change
     */
    public AnalyticsInsightDTO detectRevenueDrop(Role.RoleName actorRole, UUID shopId, DateRange current,
            DateRange previous) {
        policy.requireManagerOrOwner(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(current, "current");
        ValidationUtils.validateNotBlank(previous, "previous");

        BigDecimal cur = sumGross(shopId, current);
        BigDecimal prev = sumGross(shopId, previous);
        BigDecimal change = percentChange(prev, cur);
        RiskLevel risk = change.compareTo(new BigDecimal("-20.00")) <= 0 ? RiskLevel.HIGH
                : (change.compareTo(new BigDecimal("-10.00")) <= 0 ? RiskLevel.MEDIUM : RiskLevel.LOW);
        String rec = risk == RiskLevel.HIGH ? "Investigate revenue drop and operational issues"
                : "Monitor revenue trend";
        return new AnalyticsInsightDTO("shop_total_gross", prev, cur, change, risk, rec);
    }

    private BigDecimal sumGross(UUID shopId, DateRange range) {
        List<DailySnapshot> snaps = dailySnapshotRepository.findByShopAndDateRange(shopId, range.startInclusive(),
                range.endInclusive());
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (DailySnapshot s : snaps) {
            total = total.add(s.getTotalGross());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentChange(BigDecimal previous, BigDecimal current) {
        previous = AnalyticsMoney.normalizeSigned(previous, "previous");
        current = AnalyticsMoney.normalizeSigned(current, "current");
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : new BigDecimal("100.00");
        }
        return current.subtract(previous).multiply(new BigDecimal("100.00")).divide(previous, 2, RoundingMode.HALF_UP);
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}

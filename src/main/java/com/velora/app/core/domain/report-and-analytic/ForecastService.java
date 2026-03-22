package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Forecasting utilities based on historical snapshots only.
 */
public class ForecastService {

    private final DailyProductSnapshotRepository productSnapshotRepository;
    private final DailySnapshotRepository dailySnapshotRepository;
    private final AnalyticsAccessPolicy policy;

    public ForecastService(DailyProductSnapshotRepository productSnapshotRepository,
            DailySnapshotRepository dailySnapshotRepository) {
        this.productSnapshotRepository = require(productSnapshotRepository, "productSnapshotRepository");
        this.dailySnapshotRepository = require(dailySnapshotRepository, "dailySnapshotRepository");
        this.policy = new AnalyticsAccessPolicy();
    }

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

        Map<UUID, VariantAcc> byVariant = new HashMap<>();
        for (DailyProductSnapshot s : snaps) {
            VariantAcc acc = byVariant.computeIfAbsent(s.getVariantId(), id -> new VariantAcc());
            acc.totalQty += s.getQtySold();
            if (acc.latestDate == null || s.getSnapshotDate().isAfter(acc.latestDate)) {
                acc.latestDate = s.getSnapshotDate();
                acc.latestStockAtMidnight = s.getStockAtMidnight();
            }
        }

        List<OutOfStockPredictionDTO> result = new ArrayList<>();
        for (Map.Entry<UUID, VariantAcc> e : byVariant.entrySet()) {
            UUID variantId = e.getKey();
            VariantAcc acc = e.getValue();
            BigDecimal avgDaily = new BigDecimal(acc.totalQty).divide(new BigDecimal(7), 2, RoundingMode.HALF_UP);
            BigDecimal estimatedDays = avgDaily.compareTo(BigDecimal.ZERO) == 0
                    ? new BigDecimal("9999.00")
                    : new BigDecimal(acc.latestStockAtMidnight).divide(avgDaily, 2, RoundingMode.HALF_UP);

            RiskLevel risk = estimatedDays.compareTo(new BigDecimal(horizonDays)) <= 0 ? RiskLevel.HIGH : RiskLevel.LOW;
            String rec = risk == RiskLevel.HIGH ? "Restock or adjust pricing/promotions"
                    : "Stock level appears healthy";
            result.add(new OutOfStockPredictionDTO(variantId, acc.latestStockAtMidnight, avgDaily, estimatedDays, risk,
                    rec));
        }

        result.sort(Comparator.comparing(OutOfStockPredictionDTO::riskLevel).reversed());
        return result;
    }

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

    private static final class VariantAcc {
        private int totalQty;
        private LocalDate latestDate;
        private int latestStockAtMidnight;
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}

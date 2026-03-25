package com.velora.app.modules.report_analyticModule.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Category trends derived from daily category snapshots only.
 */
public class CategoryAnalyticsService {

    private final DailyCategorySnapshotRepository categorySnapshotRepository;
    private final AnalyticsAccessPolicy policy;

    public CategoryAnalyticsService(DailyCategorySnapshotRepository categorySnapshotRepository) {
        this.categorySnapshotRepository = require(categorySnapshotRepository, "categorySnapshotRepository");
        this.policy = new AnalyticsAccessPolicy();
    }

    public List<CategoryTrendDTO> getCategoryTrends(Role.RoleName actorRole, UUID shopId, LocalDate startInclusive,
            LocalDate endInclusive) {
        policy.requireManagerOrOwner(actorRole);
        DateRange range = new DateRange(startInclusive, endInclusive);
        ValidationUtils.validateUUID(shopId, "shopId");

        List<DailyCategorySnapshot> snaps = categorySnapshotRepository.findByShopAndDateRange(shopId,
                range.startInclusive(),
                range.endInclusive());
        Map<UUID, Acc> byCategory = new HashMap<>();
        for (DailyCategorySnapshot s : snaps) {
            Acc acc = byCategory.computeIfAbsent(s.getCategoryId(), id -> new Acc());
            acc.gross = acc.gross.add(s.getCatGrossRevenue());
            acc.profit = acc.profit.add(s.getCatNetProfit());
            acc.itemsSold += s.getCatItemsSold();
        }

        List<CategoryTrendDTO> result = new ArrayList<>();
        for (Map.Entry<UUID, Acc> e : byCategory.entrySet()) {
            result.add(
                    new CategoryTrendDTO(e.getKey(), e.getValue().gross, e.getValue().profit, e.getValue().itemsSold));
        }
        return result;
    }

    public List<AnalyticsInsightDTO> comparePeriods(Role.RoleName actorRole, UUID shopId, DateRange current,
            DateRange previous) {
        policy.requireManagerOrOwner(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(current, "current");
        ValidationUtils.validateNotBlank(previous, "previous");

        Map<UUID, BigDecimal> currentGross = sumGrossByCategory(shopId, current);
        Map<UUID, BigDecimal> previousGross = sumGrossByCategory(shopId, previous);

        List<AnalyticsInsightDTO> insights = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> e : currentGross.entrySet()) {
            UUID categoryId = e.getKey();
            BigDecimal cur = e.getValue();
            BigDecimal prev = previousGross.getOrDefault(categoryId, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            BigDecimal changePercent = percentChange(prev, cur);
            RiskLevel risk = changePercent.compareTo(new BigDecimal("-20.00")) <= 0 ? RiskLevel.HIGH
                    : (changePercent.compareTo(new BigDecimal("-10.00")) <= 0 ? RiskLevel.MEDIUM : RiskLevel.LOW);
            String rec = risk == RiskLevel.HIGH ? "Investigate category decline drivers" : "Monitor category trend";
            insights.add(new AnalyticsInsightDTO("category_gross:" + categoryId, prev, cur, changePercent, risk, rec));
        }
        return insights;
    }

    private Map<UUID, BigDecimal> sumGrossByCategory(UUID shopId, DateRange range) {
        List<DailyCategorySnapshot> snaps = categorySnapshotRepository.findByShopAndDateRange(shopId,
                range.startInclusive(),
                range.endInclusive());
        Map<UUID, BigDecimal> totals = new HashMap<>();
        for (DailyCategorySnapshot s : snaps) {
            totals.merge(s.getCategoryId(), s.getCatGrossRevenue(), BigDecimal::add);
        }
        for (Map.Entry<UUID, BigDecimal> e : totals.entrySet()) {
            totals.put(e.getKey(), e.getValue().setScale(2, RoundingMode.HALF_UP));
        }
        return totals;
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

    private static final class Acc {
        private BigDecimal gross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal profit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private int itemsSold;
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}


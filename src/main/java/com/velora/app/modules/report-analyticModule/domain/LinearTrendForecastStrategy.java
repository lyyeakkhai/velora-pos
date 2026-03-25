package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Forecast strategy that uses simple linear regression on {@code qtySold} values
 * to estimate the daily sales trend and predict days until out of stock.
 *
 * <p>For each variant, the strategy fits a line y = a + b*x over the ordered
 * snapshots (x = 0, 1, 2, …) and uses the slope {@code b} as the projected
 * daily demand. When the slope is non-positive the average daily qty is used
 * as a fallback to avoid nonsensical negative demand.
 *
 * <p>Requirements: 23.2
 */
public class LinearTrendForecastStrategy implements ForecastStrategy {

    private static final BigDecimal FALLBACK_DAYS = new BigDecimal("9999.00");
    private static final int DEFAULT_HORIZON_DAYS = 7;

    @Override
    public String getForecastType() {
        return "LINEAR_TREND";
    }

    @Override
    public List<OutOfStockPredictionDTO> predict(List<DailyProductSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return new ArrayList<>();
        }

        // Group snapshots by variantId, sorted by snapshotDate ascending
        Map<UUID, List<DailyProductSnapshot>> byVariant = new HashMap<>();
        for (DailyProductSnapshot s : snapshots) {
            byVariant.computeIfAbsent(s.getVariantId(), id -> new ArrayList<>()).add(s);
        }

        List<OutOfStockPredictionDTO> result = new ArrayList<>();

        for (Map.Entry<UUID, List<DailyProductSnapshot>> entry : byVariant.entrySet()) {
            UUID variantId = entry.getKey();
            List<DailyProductSnapshot> variantSnaps = entry.getValue();
            variantSnaps.sort(Comparator.comparing(DailyProductSnapshot::getSnapshotDate));

            int n = variantSnaps.size();

            // Compute linear regression: y = a + b*x, x in [0, n-1]
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            for (int i = 0; i < n; i++) {
                double x = i;
                double y = variantSnaps.get(i).getQtySold();
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
            }

            double denominator = n * sumX2 - sumX * sumX;
            double slope = denominator == 0 ? 0 : (n * sumXY - sumX * sumY) / denominator;

            // Use slope as projected daily demand; fall back to average if slope <= 0
            double projectedDailyDemand = slope > 0 ? slope : (n > 0 ? sumY / n : 0);

            BigDecimal avgDailyQtySold = BigDecimal.valueOf(projectedDailyDemand)
                    .setScale(2, RoundingMode.HALF_UP);

            // Latest stock level
            DailyProductSnapshot latest = variantSnaps.get(n - 1);
            int stockAtMidnight = latest.getStockAtMidnight();

            BigDecimal estimatedDays = avgDailyQtySold.compareTo(BigDecimal.ZERO) == 0
                    ? FALLBACK_DAYS
                    : new BigDecimal(stockAtMidnight).divide(avgDailyQtySold, 2, RoundingMode.HALF_UP);

            RiskLevel risk = estimatedDays.compareTo(new BigDecimal(DEFAULT_HORIZON_DAYS)) <= 0
                    ? RiskLevel.HIGH : RiskLevel.LOW;
            String rec = risk == RiskLevel.HIGH
                    ? "Restock or adjust pricing/promotions"
                    : "Stock level appears healthy";

            result.add(new OutOfStockPredictionDTO(
                    variantId, stockAtMidnight, avgDailyQtySold, estimatedDays, risk, rec));
        }

        result.sort(Comparator.comparing(OutOfStockPredictionDTO::riskLevel).reversed());
        return result;
    }
}

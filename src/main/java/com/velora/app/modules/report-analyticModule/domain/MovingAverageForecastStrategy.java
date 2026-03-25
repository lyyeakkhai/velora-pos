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
 * Forecast strategy that uses a 7-day moving average on {@code qtySold} values
 * to estimate daily demand and predict days until out of stock.
 *
 * <p>For each variant the strategy takes up to the last 7 snapshots (sorted by
 * date ascending) and averages their {@code qtySold}. If fewer than 7 snapshots
 * are available, the average is computed over however many exist.
 *
 * <p>Requirements: 23.3
 */
public class MovingAverageForecastStrategy implements ForecastStrategy {

    private static final int WINDOW = 7;
    private static final BigDecimal FALLBACK_DAYS = new BigDecimal("9999.00");
    private static final int DEFAULT_HORIZON_DAYS = 7;

    @Override
    public String getForecastType() {
        return "MOVING_AVERAGE";
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

            // Take the last WINDOW snapshots (or all if fewer)
            int windowStart = Math.max(0, n - WINDOW);
            int windowSize = n - windowStart;

            int totalQty = 0;
            for (int i = windowStart; i < n; i++) {
                totalQty += variantSnaps.get(i).getQtySold();
            }

            BigDecimal avgDailyQtySold = new BigDecimal(totalQty)
                    .divide(new BigDecimal(windowSize), 2, RoundingMode.HALF_UP);

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

package com.velora.app.core.domain.reportandanalytic;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Read port for inventory midnight stock used by analytics.
 */
public interface InventoryAnalyticsReadRepository {

    List<StockAtMidnightFact> findStockAtMidnight(UUID shopId, LocalDate snapshotDate);
}

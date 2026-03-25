package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Read port for inventory midnight stock used by analytics.
 */
public interface InventoryAnalyticsReadRepository {

    List<StockAtMidnightFact> findStockAtMidnight(UUID shopId, LocalDate snapshotDate);
}


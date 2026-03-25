package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.velora.app.common.AbstractSnapshot;

/**
 * Write-once daily snapshot aggregated at category level.
 * Requirements: 7.3
 */
public final class DailyCategorySnapshot extends AbstractSnapshot {

    private final UUID categoryId;
    private final BigDecimal catGrossRevenue;
    private final BigDecimal catNetProfit;
    private final int catItemsSold;

    public DailyCategorySnapshot(UUID snapshotId, LocalDate snapshotDate, UUID shopId,
            UUID categoryId, BigDecimal catGrossRevenue, BigDecimal catNetProfit, int catItemsSold) {
        super(snapshotId, snapshotDate, shopId);
        this.categoryId = requireUuid(categoryId, "categoryId");
        this.catGrossRevenue = AnalyticsMoney.normalizeNonNegative(catGrossRevenue, "catGrossRevenue");
        this.catNetProfit = AnalyticsMoney.normalizeSigned(catNetProfit, "catNetProfit");
        this.catItemsSold = requireNonNegativeInt(catItemsSold, "catItemsSold");
    }

    public UUID getCategoryId() { return categoryId; }
    public BigDecimal getCatGrossRevenue() { return catGrossRevenue; }
    public BigDecimal getCatNetProfit() { return catNetProfit; }
    public int getCatItemsSold() { return catItemsSold; }

    private static UUID requireUuid(UUID id, String fieldName) {
        if (id == null) throw new IllegalArgumentException(fieldName + " must not be null");
        return id;
    }

    private static int requireNonNegativeInt(int value, String fieldName) {
        if (value < 0) throw new IllegalArgumentException(fieldName + " must be >= 0");
        return value;
    }
}

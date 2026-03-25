package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.velora.app.common.AbstractSnapshot;

/**
 * High-level daily snapshot aggregated for a shop within an org.
 * Requirements: 7.3
 */
public final class DailySnapshot extends AbstractSnapshot {

    private final UUID orgId;
    private final BigDecimal totalGross;
    private final BigDecimal totalProfit;
    private final int orderCount;

    public DailySnapshot(UUID snapshotId, LocalDate snapshotDate, UUID shopId,
            UUID orgId, BigDecimal totalGross, BigDecimal totalProfit, int orderCount) {
        super(snapshotId, snapshotDate, shopId);
        this.orgId = requireUuid(orgId, "orgId");
        this.totalGross = AnalyticsMoney.normalizeNonNegative(totalGross, "totalGross");
        this.totalProfit = AnalyticsMoney.normalizeSigned(totalProfit, "totalProfit");
        this.orderCount = requireNonNegativeInt(orderCount, "orderCount");
    }

    public UUID getOrgId() { return orgId; }
    public BigDecimal getTotalGross() { return totalGross; }
    public BigDecimal getTotalProfit() { return totalProfit; }
    public int getOrderCount() { return orderCount; }

    private static UUID requireUuid(UUID id, String fieldName) {
        if (id == null) throw new IllegalArgumentException(fieldName + " must not be null");
        return id;
    }

    private static int requireNonNegativeInt(int value, String fieldName) {
        if (value < 0) throw new IllegalArgumentException(fieldName + " must be >= 0");
        return value;
    }
}

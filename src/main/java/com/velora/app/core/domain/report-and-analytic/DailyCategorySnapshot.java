package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Write-once daily snapshot aggregated at category level.
 */
public final class DailyCategorySnapshot {

    private final UUID snapshotId;
    private final LocalDate snapshotDate;
    private final UUID categoryId;
    private final UUID shopId;
    private final BigDecimal catGrossRevenue;
    private final BigDecimal catNetProfit;
    private final int catItemsSold;
    private final LocalDateTime createdAt;

    public DailyCategorySnapshot(UUID snapshotId, LocalDate snapshotDate, UUID categoryId, UUID shopId,
            BigDecimal catGrossRevenue, BigDecimal catNetProfit, int catItemsSold, LocalDateTime createdAt) {
        this.snapshotId = requireUuid(snapshotId, "snapshotId");
        this.snapshotDate = requireDate(snapshotDate, "snapshotDate");
        this.categoryId = requireUuid(categoryId, "categoryId");
        this.shopId = requireUuid(shopId, "shopId");
        this.catGrossRevenue = AnalyticsMoney.normalizeNonNegative(catGrossRevenue, "catGrossRevenue");
        this.catNetProfit = AnalyticsMoney.normalizeSigned(catNetProfit, "catNetProfit");
        this.catItemsSold = requireNonNegativeInt(catItemsSold, "catItemsSold");
        this.createdAt = requireDateTime(createdAt, "createdAt");
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public BigDecimal getCatGrossRevenue() {
        return catGrossRevenue;
    }

    public BigDecimal getCatNetProfit() {
        return catNetProfit;
    }

    public int getCatItemsSold() {
        return catItemsSold;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailyCategorySnapshot)) {
            return false;
        }
        DailyCategorySnapshot that = (DailyCategorySnapshot) o;
        return Objects.equals(snapshotId, that.snapshotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotId);
    }

    private static UUID requireUuid(UUID id, String fieldName) {
        ValidationUtils.validateUUID(id, fieldName);
        return id;
    }

    private static LocalDate requireDate(LocalDate date, String fieldName) {
        ValidationUtils.validateNotBlank(date, fieldName);
        return date;
    }

    private static LocalDateTime requireDateTime(LocalDateTime dateTime, String fieldName) {
        ValidationUtils.validateNotBlank(dateTime, fieldName);
        return dateTime;
    }

    private static int requireNonNegativeInt(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return value;
    }
}

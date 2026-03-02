package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * High-level daily snapshot aggregated for a shop within an org.
 */
public final class DailySnapshot {

    private final UUID snapshotId;
    private final LocalDate snapshotDate;
    private final UUID orgId;
    private final UUID shopId;
    private final BigDecimal totalGross;
    private final BigDecimal totalProfit;
    private final int orderCount;
    private final LocalDateTime createdAt;

    public DailySnapshot(UUID snapshotId, LocalDate snapshotDate, UUID orgId, UUID shopId, BigDecimal totalGross,
            BigDecimal totalProfit, int orderCount, LocalDateTime createdAt) {
        this.snapshotId = requireUuid(snapshotId, "snapshotId");
        this.snapshotDate = requireDate(snapshotDate, "snapshotDate");
        this.orgId = requireUuid(orgId, "orgId");
        this.shopId = requireUuid(shopId, "shopId");
        this.totalGross = AnalyticsMoney.normalizeNonNegative(totalGross, "totalGross");
        this.totalProfit = AnalyticsMoney.normalizeSigned(totalProfit, "totalProfit");
        this.orderCount = requireNonNegativeInt(orderCount, "orderCount");
        this.createdAt = requireDateTime(createdAt, "createdAt");
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public BigDecimal getTotalGross() {
        return totalGross;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailySnapshot)) {
            return false;
        }
        DailySnapshot that = (DailySnapshot) o;
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

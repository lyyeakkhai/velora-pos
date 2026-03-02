package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Atomic, write-once daily snapshot for a product variant.
 */
public final class DailyProductSnapshot {

    private final UUID snapshotId;
    private final LocalDate snapshotDate;
    private final UUID productId;
    private final UUID variantId;
    private final UUID sellerId;
    private final UUID categoryId;
    private final UUID shopId;
    private final int qtySold;
    private final BigDecimal baseCostPrice;
    private final BigDecimal unitSalePrice;
    private final int stockAtMidnight;
    private final LocalDateTime createdAt;

    public DailyProductSnapshot(UUID snapshotId, LocalDate snapshotDate, UUID productId, UUID variantId, UUID sellerId,
            UUID categoryId, UUID shopId, int qtySold, BigDecimal baseCostPrice, BigDecimal unitSalePrice,
            int stockAtMidnight, LocalDateTime createdAt) {
        this.snapshotId = requireUuid(snapshotId, "snapshotId");
        this.snapshotDate = requireDate(snapshotDate, "snapshotDate");
        this.productId = requireUuid(productId, "productId");
        this.variantId = requireUuid(variantId, "variantId");
        this.sellerId = requireUuid(sellerId, "sellerId");
        this.categoryId = requireUuid(categoryId, "categoryId");
        this.shopId = requireUuid(shopId, "shopId");
        this.qtySold = requireNonNegativeInt(qtySold, "qtySold");
        this.baseCostPrice = AnalyticsMoney.normalizeNonNegative(baseCostPrice, "baseCostPrice");
        this.unitSalePrice = AnalyticsMoney.normalizeNonNegative(unitSalePrice, "unitSalePrice");
        this.stockAtMidnight = requireNonNegativeInt(stockAtMidnight, "stockAtMidnight");
        this.createdAt = requireDateTime(createdAt, "createdAt");
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public int getQtySold() {
        return qtySold;
    }

    public BigDecimal getBaseCostPrice() {
        return baseCostPrice;
    }

    public BigDecimal getUnitSalePrice() {
        return unitSalePrice;
    }

    public int getStockAtMidnight() {
        return stockAtMidnight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailyProductSnapshot)) {
            return false;
        }
        DailyProductSnapshot that = (DailyProductSnapshot) o;
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

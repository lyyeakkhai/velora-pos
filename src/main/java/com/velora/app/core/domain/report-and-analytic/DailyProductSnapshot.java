package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.velora.app.common.AbstractSnapshot;

/**
 * Atomic, write-once daily snapshot for a product variant.
 * Requirements: 7.3
 */
public final class DailyProductSnapshot extends AbstractSnapshot {

    private final UUID productId;
    private final UUID variantId;
    private final UUID sellerId;
    private final UUID categoryId;
    private final int qtySold;
    private final BigDecimal baseCostPrice;
    private final BigDecimal unitSalePrice;
    private final int stockAtMidnight;

    public DailyProductSnapshot(UUID snapshotId, LocalDate snapshotDate, UUID shopId,
            UUID productId, UUID variantId, UUID sellerId, UUID categoryId,
            int qtySold, BigDecimal baseCostPrice, BigDecimal unitSalePrice, int stockAtMidnight) {
        super(snapshotId, snapshotDate, shopId);
        this.productId = requireUuid(productId, "productId");
        this.variantId = requireUuid(variantId, "variantId");
        this.sellerId = requireUuid(sellerId, "sellerId");
        this.categoryId = requireUuid(categoryId, "categoryId");
        this.qtySold = requireNonNegativeInt(qtySold, "qtySold");
        this.baseCostPrice = AnalyticsMoney.normalizeNonNegative(baseCostPrice, "baseCostPrice");
        this.unitSalePrice = AnalyticsMoney.normalizeNonNegative(unitSalePrice, "unitSalePrice");
        this.stockAtMidnight = requireNonNegativeInt(stockAtMidnight, "stockAtMidnight");
    }

    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public UUID getSellerId() { return sellerId; }
    public UUID getCategoryId() { return categoryId; }
    public int getQtySold() { return qtySold; }
    public BigDecimal getBaseCostPrice() { return baseCostPrice; }
    public BigDecimal getUnitSalePrice() { return unitSalePrice; }
    public int getStockAtMidnight() { return stockAtMidnight; }

    private static UUID requireUuid(UUID id, String fieldName) {
        if (id == null) throw new IllegalArgumentException(fieldName + " must not be null");
        return id;
    }

    private static int requireNonNegativeInt(int value, String fieldName) {
        if (value < 0) throw new IllegalArgumentException(fieldName + " must be >= 0");
        return value;
    }
}

package com.velora.app.modules.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a daily aggregated snapshot for a shop.
 * 
 * <p>
 * DailySnapshot is the high-level analytics aggregate that tracks
 * shop performance metrics. It is immutable once created.
 * 
 * <p>
 * Metrics tracked:
 * <ul>
 * <li>Total gross revenue</li>
 * <li>Total net profit</li>
 * <li>Order count</li>
 * </ul>
 */
public class DailySnapshot extends AbstractAuditableEntity {

    private final LocalDate snapshotDate;
    private final UUID shopId;
    private final BigDecimal totalGross;
    private final BigDecimal totalProfit;
    private final int orderCount;
    private final int itemCount;
    private final int uniqueCustomers;
    private final LocalDateTime createdAt;

    /**
     * Creates a new DailySnapshot.
     *
     * @param snapshotId      The unique snapshot identifier
     * @param snapshotDate    The date this snapshot represents
     * @param shopId          The shop identifier
     * @param totalGross      Total gross revenue
     * @param totalProfit     Total net profit
     * @param orderCount      Number of orders
     * @param itemCount       Number of items sold
     * @param uniqueCustomers Number of unique customers
     */
    public DailySnapshot(UUID snapshotId, LocalDate snapshotDate, UUID shopId,
            BigDecimal totalGross, BigDecimal totalProfit,
            int orderCount, int itemCount, int uniqueCustomers) {
        super(snapshotId);
        ValidationUtils.validateNotBlank(snapshotDate, "snapshotDate");
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(totalGross, "totalGross");
        ValidationUtils.validateNotBlank(totalProfit, "totalProfit");
        ValidationUtils.validatePositiveInteger(orderCount, "orderCount");
        ValidationUtils.validatePositiveInteger(itemCount, "itemCount");
        ValidationUtils.validateNonNegativeInteger(uniqueCustomers, "uniqueCustomers");

        this.snapshotDate = snapshotDate;
        this.shopId = shopId;
        this.totalGross = ValidationUtils.normalizeMoney(totalGross, "totalGross");
        this.totalProfit = ValidationUtils.normalizeMoney(totalProfit, "totalProfit");
        this.orderCount = orderCount;
        this.itemCount = itemCount;
        this.uniqueCustomers = uniqueCustomers;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Gets the snapshot date.
     */
    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    /**
     * Gets the shop identifier.
     */
    public UUID getShopId() {
        return shopId;
    }

    /**
     * Gets the total gross revenue.
     */
    public BigDecimal getTotalGross() {
        return totalGross;
    }

    /**
     * Gets the total net profit.
     */
    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    /**
     * Gets the order count.
     */
    public int getOrderCount() {
        return orderCount;
    }

    /**
     * Gets the item count.
     */
    public int getItemCount() {
        return itemCount;
    }

    /**
     * Gets the unique customer count.
     */
    public int getUniqueCustomers() {
        return uniqueCustomers;
    }

    /**
     * Gets the creation timestamp.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Calculates the average order value.
     */
    public BigDecimal getAverageOrderValue() {
        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalGross.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates the profit margin percentage.
     */
    public BigDecimal getProfitMargin() {
        if (totalGross.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalProfit.divide(totalGross, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates average items per order.
     */
    public BigDecimal getAverageItemsPerOrder() {
        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(itemCount)
                .divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "DailySnapshot{" +
                "id=" + getId() +
                ", date=" + snapshotDate +
                ", shopId=" + shopId +
                ", gross=" + totalGross +
                ", profit=" + totalProfit +
                ", orders=" + orderCount +
                '}';
    }
}

package com.velora.app.modules.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a daily aggregated revenue snapshot for the entire platform.
 * 
 * <p>
 * PlatformRevenueSnapshot tracks platform-level revenue metrics for
 * governance and audit compliance.
 * 
 * <p>
 * Status lifecycle:
 * <ul>
 * <li>DRAFT → FINALIZED (admin approval)</li>
 * <li>FINALIZED → LOCKED (system lock)</li>
 * </ul>
 */
public class PlatformRevenueSnapshot extends AbstractAuditableEntity {

    /**
     * Snapshot status lifecycle states
     */
    public enum Status {
        DRAFT, // Being calculated
        FINALIZED, // Approved by admin
        LOCKED // Immutable (terminal)
    }

    private final LocalDate snapshotDate;
    private Status status;
    private BigDecimal subscriptionRevenue;
    private BigDecimal transactionRevenue;
    private BigDecimal totalRevenue;
    private BigDecimal infrastructureCost;
    private BigDecimal netProfit;
    private int activePayingShops;
    private int activePayingUsers;
    private String checksum; // For audit compliance
    private LocalDateTime finalizedAt;
    private LocalDateTime lockedAt;

    /**
     * Creates a new PlatformRevenueSnapshot in DRAFT status.
     *
     * @param snapshotId   The unique snapshot identifier
     * @param snapshotDate The date this snapshot represents
     */
    public PlatformRevenueSnapshot(UUID snapshotId, LocalDate snapshotDate) {
        super(snapshotId);
        ValidationUtils.validateNotBlank(snapshotDate, "snapshotDate");

        this.snapshotDate = snapshotDate;
        this.status = Status.DRAFT;
        this.subscriptionRevenue = BigDecimal.ZERO;
        this.transactionRevenue = BigDecimal.ZERO;
        this.totalRevenue = BigDecimal.ZERO;
        this.infrastructureCost = BigDecimal.ZERO;
        this.netProfit = BigDecimal.ZERO;
        this.activePayingShops = 0;
        this.activePayingUsers = 0;
        this.checksum = null;
        this.finalizedAt = null;
        this.lockedAt = null;
    }

    /**
     * Gets the snapshot date.
     */
    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    /**
     * Gets the current status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the subscription revenue.
     */
    public BigDecimal getSubscriptionRevenue() {
        return subscriptionRevenue;
    }

    /**
     * Gets the transaction revenue.
     */
    public BigDecimal getTransactionRevenue() {
        return transactionRevenue;
    }

    /**
     * Gets the total revenue.
     */
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * Gets the infrastructure cost.
     */
    public BigDecimal getInfrastructureCost() {
        return infrastructureCost;
    }

    /**
     * Gets the net profit.
     */
    public BigDecimal getNetProfit() {
        return netProfit;
    }

    /**
     * Gets the active paying shops count.
     */
    public int getActivePayingShops() {
        return activePayingShops;
    }

    /**
     * Gets the active paying users count.
     */
    public int getActivePayingUsers() {
        return activePayingUsers;
    }

    /**
     * Gets the checksum.
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Gets the finalized timestamp.
     */
    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    /**
     * Gets the locked timestamp.
     */
    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    /**
     * Checks if the snapshot is finalized.
     */
    public boolean isFinalized() {
        return status == Status.FINALIZED || status == Status.LOCKED;
    }

    /**
     * Checks if the snapshot is locked.
     */
    public boolean isLocked() {
        return status == Status.LOCKED;
    }

    /**
     * Sets the revenue metrics.
     *
     * @param subscriptionRevenue Revenue from subscriptions
     * @param transactionRevenue  Revenue from transactions
     * @param infrastructureCost  Platform infrastructure costs
     * @param activePayingShops   Number of paying shops
     * @param activePayingUsers   Number of paying users
     * @throws IllegalStateException if not in DRAFT status
     */
    public void setMetrics(BigDecimal subscriptionRevenue, BigDecimal transactionRevenue,
            BigDecimal infrastructureCost, int activePayingShops, int activePayingUsers) {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Can only set metrics for DRAFT snapshots");
        }

        this.subscriptionRevenue = ValidationUtils.normalizeMoney(subscriptionRevenue, "subscriptionRevenue");
        this.transactionRevenue = ValidationUtils.normalizeMoney(transactionRevenue, "transactionRevenue");
        this.infrastructureCost = ValidationUtils.normalizeMoney(infrastructureCost, "infrastructureCost");
        this.totalRevenue = this.subscriptionRevenue.add(this.transactionRevenue);
        this.netProfit = this.totalRevenue.subtract(this.infrastructureCost);
        this.activePayingShops = activePayingShops;
        this.activePayingUsers = activePayingUsers;

        // Generate checksum for audit compliance
        this.checksum = generateChecksum();

        touch();
    }

    /**
     * Finalizes the snapshot (admin approval required).
     *
     * @throws IllegalStateException if not in DRAFT status
     */
    public void finalize() {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Can only finalize DRAFT snapshots");
        }
        if (checksum == null) {
            throw new IllegalStateException("Cannot finalize snapshot without metrics");
        }

        this.status = Status.FINALIZED;
        this.finalizedAt = LocalDateTime.now();
        touch();
    }

    /**
     * Locks the snapshot (system action).
     *
     * @throws IllegalStateException if not in FINALIZED status
     */
    public void lock() {
        if (status != Status.FINALIZED) {
            throw new IllegalStateException("Can only lock FINALIZED snapshots");
        }

        this.status = Status.LOCKED;
        this.lockedAt = LocalDateTime.now();
        touch();
    }

    /**
     * Generates a checksum for audit compliance.
     */
    private String generateChecksum() {
        String data = String.format("%s|%s|%s|%s|%s|%d|%d",
                snapshotDate,
                subscriptionRevenue.toPlainString(),
                transactionRevenue.toPlainString(),
                infrastructureCost.toPlainString(),
                netProfit.toPlainString(),
                activePayingShops,
                activePayingUsers);

        // Simple hash for demonstration - in production, use SHA-256
        return Integer.toHexString(data.hashCode());
    }

    @Override
    public String toString() {
        return "PlatformRevenueSnapshot{" +
                "id=" + getId() +
                ", date=" + snapshotDate +
                ", status=" + status +
                ", totalRevenue=" + totalRevenue +
                ", netProfit=" + netProfit +
                '}';
    }
}

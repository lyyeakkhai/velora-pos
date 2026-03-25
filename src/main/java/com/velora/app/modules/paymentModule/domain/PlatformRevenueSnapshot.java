package com.velora.app.modules.paymentModule.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Daily aggregated revenue snapshot for the platform.
 * <p>
 * Maps to PLATFORM_REVENUE_SNAPSHOTS.
 */
public class PlatformRevenueSnapshot {

    private UUID platformSnapId;
    private LocalDate snapshotDate;
    private BigDecimal totalRevenue;
    private Integer activePayingShops;
    private UUID lastInvoiceId;

    public PlatformRevenueSnapshot(LocalDate snapshotDate, Integer activePayingShops) {
        setPlatformSnapId(UUID.randomUUID());
        setSnapshotDate(snapshotDate);
        setActivePayingShops(activePayingShops);
        setTotalRevenue(BigDecimal.ZERO);
        setLastInvoiceId(null);
    }

    public UUID getPlatformSnapId() {
        return platformSnapId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public Integer getActivePayingShops() {
        return activePayingShops;
    }

    public UUID getLastInvoiceId() {
        return lastInvoiceId;
    }

    /**
     * Aggregates an ISSUED invoice into this snapshot.
     */
    public void addInvoice(Invoice invoice) {
        ValidationUtils.validateNotBlank(invoice, "invoice");
        if (invoice.getStatus() != InvoiceStatus.ISSUED) {
            throw new IllegalArgumentException("Only ISSUED invoices can be aggregated");
        }
        invoice.verifyTotal();
        setTotalRevenue(totalRevenue.add(invoice.getTotalAmount()));
        setLastInvoiceId(invoice.getInvoiceId());
    }

    /**
     * Convenience factory for computing a snapshot from a list of invoices.
     */
    public static PlatformRevenueSnapshot fromInvoices(LocalDate snapshotDate, Integer activePayingShops,
            List<Invoice> invoices) {
        PlatformRevenueSnapshot snapshot = new PlatformRevenueSnapshot(snapshotDate, activePayingShops);
        if (invoices == null) {
            return snapshot;
        }
        for (Invoice invoice : invoices) {
            snapshot.addInvoice(invoice);
        }
        return snapshot;
    }

    private void setPlatformSnapId(UUID platformSnapId) {
        ValidationUtils.validateUUID(platformSnapId, "platformSnapId");
        this.platformSnapId = platformSnapId;
    }

    private void setSnapshotDate(LocalDate snapshotDate) {
        ValidationUtils.validateNotBlank(snapshotDate, "snapshotDate");
        this.snapshotDate = snapshotDate;
    }

    private void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = ValidationUtils.normalizeMoney(totalRevenue, "totalRevenue");
    }

    private void setActivePayingShops(Integer activePayingShops) {
        ValidationUtils.validateNonNegativeInteger(activePayingShops, "activePayingShops");
        this.activePayingShops = activePayingShops;
    }

    private void setLastInvoiceId(UUID lastInvoiceId) {
        if (lastInvoiceId == null) {
            this.lastInvoiceId = null;
            return;
        }
        ValidationUtils.validateUUID(lastInvoiceId, "invoiceId");
        this.lastInvoiceId = lastInvoiceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlatformRevenueSnapshot)) {
            return false;
        }
        PlatformRevenueSnapshot that = (PlatformRevenueSnapshot) o;
        return Objects.equals(platformSnapId, that.platformSnapId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platformSnapId);
    }

    @Override
    public String toString() {
        return "PlatformRevenueSnapshot{" +
                "platformSnapId=" + platformSnapId +
                ", snapshotDate=" + snapshotDate +
                ", totalRevenue=" + totalRevenue +
                ", activePayingShops=" + activePayingShops +
                ", lastInvoiceId=" + lastInvoiceId +
                '}';
    }
}

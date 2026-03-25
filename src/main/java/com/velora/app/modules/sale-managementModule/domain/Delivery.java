package com.velora.app.modules.sale_managementModule.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Delivery lifecycle for a paid order.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class Delivery extends AbstractAuditableEntity {

    private UUID orderId;
    private DeliveryStatus status;
    private String address;
    private LocalDateTime completedAt;
    private String failReason;

    /**
     * Creates a delivery in PENDING status.
     */
    public Delivery(UUID orderId, String address) {
        super(UUID.randomUUID());
        setOrderId(orderId);
        setAddress(address);
        setStatus(DeliveryStatus.PENDING);
        this.completedAt = null;
        this.failReason = null;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getFailReason() {
        return failReason;
    }

    /**
     * Dispatches the delivery (PENDING -> IN_TRANSIT).
     */
    public void dispatch() {
        requireNotFailed();
        if (status != DeliveryStatus.PENDING) {
            throw new IllegalStateException("Illegal delivery transition from " + status);
        }
        setStatus(DeliveryStatus.IN_TRANSIT);
        touch();
    }

    /**
     * Completes the delivery (IN_TRANSIT -> DELIVERED).
     */
    public void complete() {
        requireNotFailed();
        if (status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException("Illegal delivery transition from " + status);
        }
        setStatus(DeliveryStatus.DELIVERED);
        this.completedAt = LocalDateTime.now();
        touch();
    }

    /**
     * Fails the delivery permanently.
     */
    public void fail(String reason) {
        if (status == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Cannot fail a delivered shipment");
        }
        if (status == DeliveryStatus.FAILED) {
            return;
        }
        ValidationUtils.validateNotBlank(reason, "reason");
        setStatus(DeliveryStatus.FAILED);
        setFailReason(reason);
        this.completedAt = LocalDateTime.now();
        touch();
    }

    public void setAddress(String address) {
        ValidationUtils.validateNotBlank(address, "address");
        this.address = address.trim();
        touch();
    }

    private void requireNotFailed() {
        if (status == DeliveryStatus.FAILED) {
            throw new IllegalStateException("Delivery is FAILED and cannot transition");
        }
    }

    private void setOrderId(UUID orderId) {
        ValidationUtils.validateUUID(orderId, "orderId");
        this.orderId = orderId;
    }

    private void setStatus(DeliveryStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setFailReason(String failReason) {
        if (failReason == null) {
            this.failReason = null;
            return;
        }
        ValidationUtils.validateNotBlank(failReason, "failReason");
        this.failReason = failReason;
    }

    @Override
    public String toString() {
        return "Delivery{id=" + getId() +
                ", orderId=" + orderId +
                ", status=" + status +
                '}';
    }
}

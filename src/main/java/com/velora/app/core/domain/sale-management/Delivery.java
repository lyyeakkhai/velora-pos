package com.velora.app.core.domain.salemanagement;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Delivery lifecycle for a paid order.
 */
public class Delivery {

    private UUID deliveryId;
    private UUID orderId;
    private DeliveryStatus status;
    private String address;
    private LocalDateTime completedAt;
    private String failReason;

    /**
     * Creates a delivery in PENDING status.
     */
    public Delivery(UUID orderId, String address) {
        setDeliveryId(UUID.randomUUID());
        setOrderId(orderId);
        setAddress(address);
        setStatus(DeliveryStatus.PENDING);
        setCompletedAt(null);
        setFailReason(null);
    }

    public UUID getDeliveryId() {
        return deliveryId;
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
        setCompletedAt(LocalDateTime.now());
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
        setCompletedAt(LocalDateTime.now());
    }

    private void requireNotFailed() {
        if (status == DeliveryStatus.FAILED) {
            throw new IllegalStateException("Delivery is FAILED and cannot transition");
        }
    }

    private void setDeliveryId(UUID deliveryId) {
        ValidationUtils.validateUUID(deliveryId, "deliveryId");
        this.deliveryId = deliveryId;
    }

    private void setOrderId(UUID orderId) {
        ValidationUtils.validateUUID(orderId, "orderId");
        this.orderId = orderId;
    }

    public void setAddress(String address) {
        ValidationUtils.validateNotBlank(address, "address");
        this.address = address.toString().trim();
    }

    private void setStatus(DeliveryStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Delivery)) {
            return false;
        }
        Delivery delivery = (Delivery) o;
        return Objects.equals(deliveryId, delivery.deliveryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryId);
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "deliveryId=" + deliveryId +
                ", orderId=" + orderId +
                ", status=" + status +
                '}';
    }
}

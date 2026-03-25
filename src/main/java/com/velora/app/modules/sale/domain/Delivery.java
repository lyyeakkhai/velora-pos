package com.velora.app.modules.sale.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a delivery/logistics record for an order.
 * 
 * <p>
 * Delivery manages the physical delivery lifecycle of a paid order.
 * 
 * <p>
 * State transitions:
 * <ul>
 * <li>PENDING → IN_TRANSIT (dispatched)</li>
 * <li>IN_TRANSIT → DELIVERED (completed)</li>
 * <li>ANY → FAILED (terminal)</li>
 * </ul>
 */
public class Delivery extends AbstractAuditableEntity {

    /**
     * Delivery status lifecycle states
     */
    public enum Status {
        PENDING, // Awaiting dispatch
        IN_TRANSIT, // On the way
        DELIVERED, // Successfully delivered
        FAILED // Delivery failed (terminal)
    }

    private final UUID orderId;
    private final String deliveryAddress;
    private Status status;
    private String trackingNumber;
    private String failureReason;
    private LocalDateTime completedAt;

    /**
     * Creates a new Delivery in PENDING status.
     *
     * @param deliveryId      The unique delivery identifier
     * @param orderId         The associated order identifier
     * @param deliveryAddress The delivery address (required)
     */
    public Delivery(UUID deliveryId, UUID orderId, String deliveryAddress) {
        super(deliveryId);
        ValidationUtils.validateUUID(orderId, "orderId");
        ValidationUtils.validateNotBlank(deliveryAddress, "deliveryAddress");

        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.status = Status.PENDING;
        this.trackingNumber = null;
        this.failureReason = null;
        this.completedAt = null;
    }

    /**
     * Gets the order identifier.
     */
    public UUID getOrderId() {
        return orderId;
    }

    /**
     * Gets the delivery address.
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * Gets the current status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the tracking number.
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }

    /**
     * Gets the failure reason.
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Gets the completion timestamp.
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Checks if delivery is pending.
     */
    public boolean isPending() {
        return status == Status.PENDING;
    }

    /**
     * Checks if delivery is in transit.
     */
    public boolean isInTransit() {
        return status == Status.IN_TRANSIT;
    }

    /**
     * Checks if delivery is completed.
     */
    public boolean isDelivered() {
        return status == Status.DELIVERED;
    }

    /**
     * Checks if delivery has failed.
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    /**
     * Checks if delivery is completed (either delivered or failed).
     */
    public boolean isCompleted() {
        return status == Status.DELIVERED || status == Status.FAILED;
    }

    /**
     * Dispatches the delivery (PENDING → IN_TRANSIT).
     *
     * @param trackingNumber The courier tracking number (optional)
     * @throws IllegalStateException if not in PENDING status
     */
    public void dispatch(String trackingNumber) {
        if (status != Status.PENDING) {
            throw new IllegalStateException(
                    "Delivery can only be dispatched from PENDING status. Current: " + status);
        }
        this.status = Status.IN_TRANSIT;
        this.trackingNumber = trackingNumber;
        touch();
    }

    /**
     * Marks delivery as completed (IN_TRANSIT → DELIVERED).
     *
     * @throws IllegalStateException if not in IN_TRANSIT status
     */
    public void complete() {
        if (status != Status.IN_TRANSIT) {
            throw new IllegalStateException(
                    "Delivery can only be completed from IN_TRANSIT status. Current: " + status);
        }
        this.status = Status.DELIVERED;
        this.completedAt = LocalDateTime.now();
        touch();
    }

    /**
     * Marks delivery as failed (ANY → FAILED).
     * This is a terminal state.
     *
     * @param reason The reason for failure
     * @throws IllegalStateException if already completed
     */
    public void fail(String reason) {
        if (isCompleted()) {
            throw new IllegalStateException(
                    "Cannot fail an already completed delivery. Current: " + status);
        }
        this.status = Status.FAILED;
        this.failureReason = reason != null ? reason.trim() : null;
        this.completedAt = LocalDateTime.now();
        touch();
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "id=" + getId() +
                ", orderId=" + orderId +
                ", status=" + status +
                ", trackingNumber='" + trackingNumber + '\'' +
                '}';
    }
}

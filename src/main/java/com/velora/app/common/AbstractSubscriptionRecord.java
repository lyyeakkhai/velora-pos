package com.velora.app.common;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for subscription purchase records.
 *
 * <p>Shared by {@code UserSubscription} and {@code ShopSubscription}. Holds all common
 * record fields and provides {@link #markRefunded()} to transition the record to a
 * refunded state. Subclasses supply the concrete status type via {@link #setRefundedStatus()}
 * and expose their own typed {@code getStatus()} getter.
 *
 * <p>Requirements: 6.1, 6.2
 */
public abstract class AbstractSubscriptionRecord {

    protected UUID subscriptionId;
    protected UUID transactionId;
    protected UUID planId;
    protected LocalDateTime startDate;
    protected LocalDateTime endDate;
    protected LocalDateTime refundDeadline;

    // --- Shared behaviour ---

    /**
     * Marks this subscription record as refunded by delegating the status transition
     * to the subclass via {@link #setRefundedStatus()}.
     *
     * <p>Requirement: 6.2
     *
     * @throws IllegalStateException if the record is already refunded or the refund
     *                               deadline has passed
     */
    public void markRefunded() {
        if (isAlreadyRefunded()) {
            throw new IllegalStateException("Already refunded");
        }
        if (refundDeadline != null && LocalDateTime.now().isAfter(refundDeadline)) {
            throw new IllegalStateException("Refund deadline has passed");
        }
        setRefundedStatus();
    }

    /**
     * Returns {@code true} if this record is already in a refunded state.
     * Subclasses implement this by checking their own status enum value.
     */
    protected abstract boolean isAlreadyRefunded();

    /**
     * Transitions the record's status to REFUNDED.
     * Subclasses set their own status enum value here.
     */
    protected abstract void setRefundedStatus();

    // --- Getters ---

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public LocalDateTime getRefundDeadline() {
        return refundDeadline;
    }

    // --- Identity ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSubscriptionRecord)) return false;
        AbstractSubscriptionRecord that = (AbstractSubscriptionRecord) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{subscriptionId=" + subscriptionId
                + ", transactionId=" + transactionId
                + ", planId=" + planId
                + ", startDate=" + startDate
                + ", endDate=" + endDate + "}";
    }
}

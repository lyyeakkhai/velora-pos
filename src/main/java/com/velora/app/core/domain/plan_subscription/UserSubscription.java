package com.velora.app.core.domain.plan_subscription;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractSubscriptionRecord;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Purchase record for a user's subscription.
 *
 * <p>Extends {@link AbstractSubscriptionRecord} for shared fields:
 * {@code subscriptionId}, {@code transactionId}, {@code planId},
 * {@code startDate}, {@code endDate}, {@code refundDeadline}.
 *
 * <p>Requirements: 6.3
 */
public class UserSubscription extends AbstractSubscriptionRecord {

    private UUID userId;
    private UserSubscriptionStatus status;
    private Integer durationMonths;

    /**
     * Creates a user subscription purchase record. startDate defaults to now.
     */
    public UserSubscription(UUID userId, UUID transactionId, SubscriptionPlan plan) {
        ValidationUtils.validateUUID(userId, "userId");
        ValidationUtils.validateUUID(transactionId, "transactionId");
        ValidationUtils.validateNotBlank(plan, "plan");

        this.subscriptionId = UUID.randomUUID();
        this.userId = userId;
        this.transactionId = transactionId;
        this.planId = plan.getPlanId();
        this.durationMonths = plan.getDurationMonths();
        this.status = UserSubscriptionStatus.ACTIVE;

        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate();
        this.refundDeadline = calculateRefundDeadline();
    }

    // --- Domain-specific getters ---

    public UUID getUserId() {
        return userId;
    }

    public UserSubscriptionStatus getStatus() {
        return status;
    }

    // --- AbstractSubscriptionRecord hooks ---

    @Override
    protected boolean isAlreadyRefunded() {
        return status == UserSubscriptionStatus.REFUNDED;
    }

    @Override
    protected void setRefundedStatus() {
        this.status = UserSubscriptionStatus.REFUNDED;
    }

    // --- Calculation helpers ---

    public LocalDateTime calculateEndDate() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        ValidationUtils.validatePositiveInteger(durationMonths, "durationMonths");
        return startDate.plusMonths(durationMonths);
    }

    public LocalDateTime calculateRefundDeadline() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        return startDate.plusDays(14);
    }

    // --- Expiration ---

    public void markExpiredIfNeeded() {
        if (status == UserSubscriptionStatus.ACTIVE
                && endDate != null
                && !endDate.isAfter(LocalDateTime.now())) {
            this.status = UserSubscriptionStatus.EXPIRED;
        }
    }

    public UserSubscriptionStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    @Override
    public String toString() {
        return "UserSubscription{" +
                "subscriptionId=" + subscriptionId +
                ", userId=" + userId +
                ", transactionId=" + transactionId +
                ", planId=" + planId +
                ", status=" + status +
                '}';
    }
}

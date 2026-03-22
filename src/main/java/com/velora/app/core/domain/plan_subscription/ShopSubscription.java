package com.velora.app.core.domain.plan_subscription;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractSubscriptionRecord;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Purchase record for a shop subscription.
 *
 * <p>Extends {@link AbstractSubscriptionRecord} for shared fields:
 * {@code subscriptionId}, {@code transactionId}, {@code planId},
 * {@code startDate}, {@code endDate}, {@code refundDeadline}.
 *
 * <p>Requirements: 6.4
 */
public class ShopSubscription extends AbstractSubscriptionRecord {

    private UUID shopId;
    private ShopSubscriptionStatus status;
    private Integer durationMonths;

    /**
     * Creates a shop subscription purchase record. startDate defaults to now.
     */
    public ShopSubscription(UUID shopId, UUID transactionId, SubscriptionPlan plan) {
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateUUID(transactionId, "transactionId");
        ValidationUtils.validateNotBlank(plan, "plan");

        this.subscriptionId = UUID.randomUUID();
        this.shopId = shopId;
        this.transactionId = transactionId;
        this.planId = plan.getPlanId();
        this.durationMonths = plan.getDurationMonths();
        this.status = ShopSubscriptionStatus.ACTIVE;

        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate();
        this.refundDeadline = calculateRefundDeadline();
    }

    // --- Domain-specific getters ---

    public UUID getShopId() {
        return shopId;
    }

    public ShopSubscriptionStatus getStatus() {
        return status;
    }

    // --- AbstractSubscriptionRecord hooks ---

    @Override
    protected boolean isAlreadyRefunded() {
        return status == ShopSubscriptionStatus.REFUNDED;
    }

    @Override
    protected void setRefundedStatus() {
        this.status = ShopSubscriptionStatus.REFUNDED;
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
        if (status == ShopSubscriptionStatus.ACTIVE
                && endDate != null
                && !endDate.isAfter(LocalDateTime.now())) {
            this.status = ShopSubscriptionStatus.EXPIRED;
        }
    }

    public ShopSubscriptionStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    @Override
    public String toString() {
        return "ShopSubscription{" +
                "subscriptionId=" + subscriptionId +
                ", shopId=" + shopId +
                ", transactionId=" + transactionId +
                ", planId=" + planId +
                ", status=" + status +
                '}';
    }
}

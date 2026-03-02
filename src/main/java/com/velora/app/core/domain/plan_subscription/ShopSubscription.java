package com.velora.app.core.domain.plan_subscription;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Purchase record for a shop subscription.
 */
public class ShopSubscription {

    private UUID subscriptionId;
    private UUID shopId;
    private UUID transactionId;
    private UUID planId;
    private ShopSubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime refundDeadline;

    private Integer durationMonths;

    /**
     * Creates a shop subscription purchase record. startDate defaults to now.
     */
    public ShopSubscription(UUID shopId, UUID transactionId, SubscriptionPlan plan) {
        setSubscriptionId(UUID.randomUUID());
        setShopId(shopId);
        setTransactionId(transactionId);
        ValidationUtils.validateNotBlank(plan, "plan");
        setPlanId(plan.getPlanId());
        setDurationMonths(plan.getDurationMonths());
        setStatus(ShopSubscriptionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public ShopSubscriptionStatus getStatus() {
        return status;
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

    public LocalDateTime calculateEndDate() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        ValidationUtils.validatePositiveInteger(durationMonths, "durationMonths");
        return startDate.plusMonths(durationMonths);
    }

    public LocalDateTime calculateRefundDeadline() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        return startDate.plusDays(14);
    }

    public void markRefunded() {
        if (status == ShopSubscriptionStatus.REFUNDED) {
            throw new IllegalStateException("Already refunded");
        }
        if (refundDeadline != null && LocalDateTime.now().isAfter(refundDeadline)) {
            throw new IllegalStateException("Refund deadline has passed");
        }
        setStatus(ShopSubscriptionStatus.REFUNDED);
    }

    public void markExpiredIfNeeded() {
        if (status == ShopSubscriptionStatus.ACTIVE
                && endDate != null
                && !endDate.isAfter(LocalDateTime.now())) {
            setStatus(ShopSubscriptionStatus.EXPIRED);
        }
    }

    public ShopSubscriptionStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    private void setSubscriptionId(UUID subscriptionId) {
        ValidationUtils.validateUUID(subscriptionId, "subscriptionId");
        this.subscriptionId = subscriptionId;
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setTransactionId(UUID transactionId) {
        ValidationUtils.validateUUID(transactionId, "transactionId");
        this.transactionId = transactionId;
    }

    private void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    private void setStatus(ShopSubscriptionStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setStartDate(LocalDateTime startDate) {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        this.startDate = startDate;
    }

    private void setEndDate(LocalDateTime endDate) {
        ValidationUtils.validateNotBlank(endDate, "endDate");
        this.endDate = endDate;
        ValidationUtils.validateStartBeforeEnd(startDate, endDate, "startDate", "endDate");
    }

    private void setRefundDeadline(LocalDateTime refundDeadline) {
        ValidationUtils.validateNotBlank(refundDeadline, "refundDeadline");
        this.refundDeadline = refundDeadline;
    }

    private void setDurationMonths(Integer durationMonths) {
        ValidationUtils.validatePositiveInteger(durationMonths, "durationMonths");
        this.durationMonths = durationMonths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShopSubscription)) {
            return false;
        }
        ShopSubscription that = (ShopSubscription) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
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

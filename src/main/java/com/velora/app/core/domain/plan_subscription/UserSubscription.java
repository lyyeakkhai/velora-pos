package com.velora.app.core.domain.plan_subscription;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Purchase record for a user's subscription.
 */
public class UserSubscription {

    private UUID subscriptionId;
    private UUID userId;
    private UUID transactionId;
    private UUID planId;
    private UserSubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime refundDeadline;

    private Integer durationMonths;

    /**
     * Creates a user subscription purchase record. startDate defaults to now.
     */
    public UserSubscription(UUID userId, UUID transactionId, SubscriptionPlan plan) {
        setSubscriptionId(UUID.randomUUID());
        setUserId(userId);
        setTransactionId(transactionId);
        ValidationUtils.validateNotBlank(plan, "plan");
        setPlanId(plan.getPlanId());
        setDurationMonths(plan.getDurationMonths());
        setStatus(UserSubscriptionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public UserSubscriptionStatus getStatus() {
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

    /**
     * Calculates endDate from startDate and plan duration.
     */
    public LocalDateTime calculateEndDate() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        ValidationUtils.validatePositiveInteger(durationMonths, "durationMonths");
        return startDate.plusMonths(durationMonths);
    }

    /**
     * Calculates refund deadline as startDate + 14 days.
     */
    public LocalDateTime calculateRefundDeadline() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        return startDate.plusDays(14);
    }

    /**
     * Marks this subscription as refunded.
     */
    public void markRefunded() {
        if (status == UserSubscriptionStatus.REFUNDED) {
            throw new IllegalStateException("Already refunded");
        }
        if (refundDeadline != null && LocalDateTime.now().isAfter(refundDeadline)) {
            throw new IllegalStateException("Refund deadline has passed");
        }
        setStatus(UserSubscriptionStatus.REFUNDED);
    }

    /**
     * Cron/job helper: marks expired if needed.
     */
    public void markExpiredIfNeeded() {
        if (status == UserSubscriptionStatus.ACTIVE
                && endDate != null
                && !endDate.isAfter(LocalDateTime.now())) {
            setStatus(UserSubscriptionStatus.EXPIRED);
        }
    }

    public UserSubscriptionStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    private void setSubscriptionId(UUID subscriptionId) {
        ValidationUtils.validateUUID(subscriptionId, "subscriptionId");
        this.subscriptionId = subscriptionId;
    }

    private void setUserId(UUID userId) {
        ValidationUtils.validateUUID(userId, "userId");
        this.userId = userId;
    }

    private void setTransactionId(UUID transactionId) {
        ValidationUtils.validateUUID(transactionId, "transactionId");
        this.transactionId = transactionId;
    }

    private void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    private void setStatus(UserSubscriptionStatus status) {
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
        if (!(o instanceof UserSubscription)) {
            return false;
        }
        UserSubscription that = (UserSubscription) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
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

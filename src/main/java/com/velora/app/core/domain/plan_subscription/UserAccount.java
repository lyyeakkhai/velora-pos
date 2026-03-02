package com.velora.app.core.domain.plan_subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a user's subscription account lifecycle.
 */
public class UserAccount {

    private UUID subscriptionId;
    private UUID userId;
    private UUID planId;
    private UUID registryId;
    private UUID transactionId;
    private UserAccountStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime refundDeadline;
    private Integer currentPlanDurationMonths;

    /**
     * Creates a user account with mandatory identifiers and an initial status.
     */
    public UserAccount(UUID userId, UUID planId, UUID registryId, UserAccountStatus status) {
        setSubscriptionId(UUID.randomUUID());
        setUserId(userId);
        setPlanId(planId);
        setRegistryId(registryId);
        setTransactionId(null);
        setStatus(status);
        setStartDate(null);
        setEndDate(null);
        setRefundDeadline(null);
        setCurrentPlanDurationMonths(null);
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public UUID getRegistryId() {
        return registryId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UserAccountStatus getStatus() {
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
     * Activates a plan for this account (sets start/end/refund deadlines).
     */
    public void activatePlan(SubscriptionPlan plan) {
        ValidationUtils.validateNotBlank(plan, "plan");
        if (!plan.isAvailable()) {
            throw new IllegalStateException("Plan is not available");
        }
        if (plan.getPayerType() != PayerType.USER) {
            throw new IllegalArgumentException("Plan payerType must be USER for a UserAccount");
        }

        setPlanId(plan.getPlanId());
        setCurrentPlanDurationMonths(plan.getDurationMonths());

        LocalDateTime now = LocalDateTime.now();
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());

        if (plan.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            setStatus(UserAccountStatus.TRIAL);
        } else {
            setStatus(UserAccountStatus.ACTIVE);
        }
    }

    /**
     * Extends the current plan by its duration.
     */
    public void extendPlan() {
        requireActiveOrTrial();
        requireDurationKnown();
        ValidationUtils.validateNotBlank(endDate, "endDate");
        setEndDate(endDate.plusMonths(currentPlanDurationMonths));
    }

    /**
     * Expires this account if currently active/trial.
     */
    public void expire() {
        if (status == UserAccountStatus.ACTIVE || status == UserAccountStatus.TRIAL) {
            setStatus(UserAccountStatus.EXPIRED);
        } else {
            throw new IllegalStateException("Cannot expire from status " + status);
        }
    }

    /**
     * Cancels the account immediately.
     */
    public void cancel() {
        if (status == UserAccountStatus.CANCELLED) {
            throw new IllegalStateException("Account already cancelled");
        }
        setStatus(UserAccountStatus.CANCELLED);
        if (startDate != null) {
            setEndDate(LocalDateTime.now());
        }
    }

    /**
     * Renews the subscription by re-activating the current plan duration.
     */
    public void renew() {
        if (status != UserAccountStatus.EXPIRED) {
            throw new IllegalStateException("Only expired accounts can be renewed");
        }
        requireDurationKnown();
        LocalDateTime now = LocalDateTime.now();
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());
        setStatus(UserAccountStatus.ACTIVE);
    }

    /**
     * Upgrades the account to a new plan.
     */
    public void upgrade(SubscriptionPlan newPlan) {
        ValidationUtils.validateNotBlank(newPlan, "newPlan");
        if (!isActive()) {
            throw new IllegalStateException("Account must be active to upgrade");
        }
        if (newPlan.getPayerType() != PayerType.USER) {
            throw new IllegalArgumentException("Plan payerType must be USER for a UserAccount");
        }
        setPlanId(newPlan.getPlanId());
        setCurrentPlanDurationMonths(newPlan.getDurationMonths());
        LocalDateTime now = LocalDateTime.now();
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());
        setStatus(UserAccountStatus.ACTIVE);
    }

    /**
     * Returns true if the account is currently active and not expired.
     */
    public boolean isActive() {
        if (status != UserAccountStatus.ACTIVE && status != UserAccountStatus.TRIAL) {
            return false;
        }
        if (startDate == null || endDate == null) {
            return false;
        }
        return endDate.isAfter(LocalDateTime.now());
    }

    /**
     * Cron/job helper: marks the account expired if needed.
     */
    public void markExpiredIfNeeded() {
        if ((status == UserAccountStatus.ACTIVE || status == UserAccountStatus.TRIAL)
                && endDate != null
                && !endDate.isAfter(LocalDateTime.now())) {
            setStatus(UserAccountStatus.EXPIRED);
        }
    }

    /**
     * Cron/job helper: checks expiration and returns current status.
     */
    public UserAccountStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    /**
     * Calculates endDate from startDate and current plan duration.
     */
    public LocalDateTime calculateEndDate() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        requireDurationKnown();
        return startDate.plusMonths(currentPlanDurationMonths);
    }

    /**
     * Calculates refund deadline as startDate + 14 days.
     */
    public LocalDateTime calculateRefundDeadline() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        return startDate.plusDays(14);
    }

    private void requireActiveOrTrial() {
        if (status != UserAccountStatus.ACTIVE && status != UserAccountStatus.TRIAL) {
            throw new IllegalStateException("Account must be ACTIVE/TRIAL");
        }
    }

    private void requireDurationKnown() {
        if (currentPlanDurationMonths == null || currentPlanDurationMonths <= 0) {
            throw new IllegalStateException("Plan duration is unknown; activatePlan must be called first");
        }
    }

    private void setSubscriptionId(UUID subscriptionId) {
        ValidationUtils.validateUUID(subscriptionId, "subscriptionId");
        this.subscriptionId = subscriptionId;
    }

    private void setUserId(UUID userId) {
        ValidationUtils.validateUUID(userId, "userId");
        this.userId = userId;
    }

    public void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    private void setRegistryId(UUID registryId) {
        ValidationUtils.validateUUID(registryId, "registryId");
        this.registryId = registryId;
    }

    /**
     * Optional transaction reference.
     */
    public void setTransactionId(UUID transactionId) {
        if (transactionId == null) {
            this.transactionId = null;
            return;
        }
        ValidationUtils.validateUUID(transactionId, "transactionId");
        this.transactionId = transactionId;
    }

    private void setStatus(UserAccountStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    private void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        if (startDate != null && endDate != null) {
            ValidationUtils.validateStartBeforeEnd(startDate, endDate, "startDate", "endDate");
        }
    }

    private void setRefundDeadline(LocalDateTime refundDeadline) {
        this.refundDeadline = refundDeadline;
    }

    private void setCurrentPlanDurationMonths(Integer currentPlanDurationMonths) {
        this.currentPlanDurationMonths = currentPlanDurationMonths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAccount)) {
            return false;
        }
        UserAccount that = (UserAccount) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "subscriptionId=" + subscriptionId +
                ", userId=" + userId +
                ", planId=" + planId +
                ", registryId=" + registryId +
                ", status=" + status +
                '}';
    }
}

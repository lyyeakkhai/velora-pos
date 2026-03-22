package com.velora.app.core.domain.plan_subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractSubscriptionAccount;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a user's subscription account lifecycle.
 *
 * <p>Extends {@link AbstractSubscriptionAccount} for shared subscription fields
 * and lifecycle helpers. Provides user-specific activation, upgrade, renew, and cancel logic.
 *
 * <p>Requirements: 5.8
 */
public class UserAccount extends AbstractSubscriptionAccount {

    private UUID userId;
    private UUID transactionId;
    private UserAccountStatus status;

    /**
     * Creates a user account with mandatory identifiers and an initial status.
     */
    public UserAccount(UUID userId, UUID planId, UUID registryId, UserAccountStatus status) {
        this.subscriptionId = UUID.randomUUID();
        ValidationUtils.validateUUID(userId, "userId");
        this.userId = userId;
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
        ValidationUtils.validateUUID(registryId, "registryId");
        this.registryId = registryId;
        this.transactionId = null;
        setStatus(status);
        this.startDate = null;
        this.endDate = null;
        this.refundDeadline = null;
        this.currentPlanDurationMonths = null;
    }

    // --- Domain-specific getters ---

    public UUID getUserId() {
        return userId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UserAccountStatus getStatus() {
        return status;
    }

    // --- AbstractSubscriptionAccount contract ---

    /**
     * Returns true if the account is currently active or trial and not expired.
     *
     * <p>Requirement: 5.8
     */
    @Override
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
     * Activates a plan for this account (sets start/end/refund deadlines).
     *
     * <p>Requirement: 5.8
     */
    @Override
    public void activatePlan(SubscriptionPlan plan) {
        ValidationUtils.validateNotBlank(plan, "plan");
        if (!plan.isAvailable()) {
            throw new IllegalStateException("Plan is not available");
        }
        if (plan.getPayerType() != PayerType.USER) {
            throw new IllegalArgumentException("Plan payerType must be USER for a UserAccount");
        }

        this.planId = plan.getPlanId();
        this.currentPlanDurationMonths = plan.getDurationMonths();

        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate(now, currentPlanDurationMonths);
        this.refundDeadline = calculateRefundDeadline(now);

        if (plan.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            setStatus(UserAccountStatus.TRIAL);
        } else {
            setStatus(UserAccountStatus.ACTIVE);
        }
    }

    /**
     * Cancels the account immediately.
     *
     * <p>Requirement: 5.8
     */
    @Override
    public void cancel() {
        if (status == UserAccountStatus.CANCELLED) {
            throw new IllegalStateException("Account already cancelled");
        }
        setStatus(UserAccountStatus.CANCELLED);
        if (startDate != null) {
            this.endDate = LocalDateTime.now();
        }
    }

    /**
     * Transitions status to EXPIRED. Called by {@link #markExpiredIfNeeded()}.
     */
    @Override
    protected void expireStatus() {
        setStatus(UserAccountStatus.EXPIRED);
    }

    // --- Domain-specific methods ---

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
     * Renews the subscription by re-activating the current plan duration.
     */
    public void renew() {
        if (status != UserAccountStatus.EXPIRED) {
            throw new IllegalStateException("Only expired accounts can be renewed");
        }
        requireDurationKnown();
        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate(now, currentPlanDurationMonths);
        this.refundDeadline = calculateRefundDeadline(now);
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
        this.planId = newPlan.getPlanId();
        this.currentPlanDurationMonths = newPlan.getDurationMonths();
        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate(now, currentPlanDurationMonths);
        this.refundDeadline = calculateRefundDeadline(now);
        setStatus(UserAccountStatus.ACTIVE);
    }

    /**
     * Extends the current plan by its duration.
     */
    public void extendPlan() {
        requireActiveOrTrial();
        requireDurationKnown();
        ValidationUtils.validateNotBlank(endDate, "endDate");
        this.endDate = endDate.plusMonths(currentPlanDurationMonths);
    }

    /**
     * Cron/job helper: checks expiration and returns current status.
     */
    public UserAccountStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    // --- Setters ---

    public void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
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

    // --- Guards ---

    private void setStatus(UserAccountStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccount)) return false;
        UserAccount that = (UserAccount) o;
        return subscriptionId != null && subscriptionId.equals(that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return subscriptionId != null ? subscriptionId.hashCode() : 0;
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
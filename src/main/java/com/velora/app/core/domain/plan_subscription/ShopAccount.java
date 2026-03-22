package com.velora.app.core.domain.plan_subscription;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractSubscriptionAccount;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a shop's subscription account lifecycle.
 *
 * <p>Extends {@link AbstractSubscriptionAccount} for shared subscription fields
 * and lifecycle helpers. Provides shop-specific activation, upgrade, renew, and cancel logic.
 *
 * <p>Requirements: 5.9
 */
public class ShopAccount extends AbstractSubscriptionAccount {

    private UUID shopId;
    private ShopAccountStatus status;
    private boolean autoRenew;

    /**
     * Creates a shop account with mandatory identifiers and initial status.
     */
    public ShopAccount(UUID shopId, UUID planId, UUID registryId, ShopAccountStatus status, boolean autoRenew) {
        this.subscriptionId = UUID.randomUUID();
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
        ValidationUtils.validateUUID(registryId, "registryId");
        this.registryId = registryId;
        setStatus(status);
        this.startDate = null;
        this.endDate = null;
        this.refundDeadline = null;
        this.autoRenew = autoRenew;
        this.currentPlanDurationMonths = null;
    }

    // --- Domain-specific getters ---

    public UUID getShopId() {
        return shopId;
    }

    public ShopAccountStatus getStatus() {
        return status;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    // --- AbstractSubscriptionAccount contract ---

    /**
     * Returns true if the shop account is currently active and not expired.
     *
     * <p>Requirement: 5.9
     */
    @Override
    public boolean isActive() {
        if (status != ShopAccountStatus.ACTIVE) {
            return false;
        }
        if (startDate == null || endDate == null) {
            return false;
        }
        return endDate.isAfter(LocalDateTime.now());
    }

    /**
     * Activates a plan for this shop (sets timestamps).
     *
     * <p>Requirement: 5.9
     */
    @Override
    public void activatePlan(SubscriptionPlan plan) {
        ValidationUtils.validateNotBlank(plan, "plan");
        if (!plan.isAvailable()) {
            throw new IllegalStateException("Plan is not available");
        }
        if (plan.getPayerType() != PayerType.SHOP) {
            throw new IllegalArgumentException("Plan payerType must be SHOP for a ShopAccount");
        }
        this.planId = plan.getPlanId();
        this.currentPlanDurationMonths = plan.getDurationMonths();
        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate(now, currentPlanDurationMonths);
        this.refundDeadline = calculateRefundDeadline(now);
        setStatus(ShopAccountStatus.ACTIVE);
    }

    /**
     * Cancels the shop account immediately.
     *
     * <p>Requirement: 5.9
     */
    @Override
    public void cancel() {
        if (status == ShopAccountStatus.CANCELLED) {
            throw new IllegalStateException("Account already cancelled");
        }
        setStatus(ShopAccountStatus.CANCELLED);
        if (startDate != null) {
            this.endDate = LocalDateTime.now();
        }
    }

    /**
     * Transitions status to EXPIRED. Called by {@link #markExpiredIfNeeded()}.
     */
    @Override
    protected void expireStatus() {
        setStatus(ShopAccountStatus.EXPIRED);
    }

    // --- Domain-specific methods ---

    /**
     * Expires this shop account.
     */
    public void expire() {
        if (status == ShopAccountStatus.ACTIVE) {
            setStatus(ShopAccountStatus.EXPIRED);
        } else {
            throw new IllegalStateException("Cannot expire from status " + status);
        }
    }

    /**
     * Renews the shop subscription. Requires auto-renew and expired status.
     */
    public void renew() {
        if (!autoRenew) {
            throw new IllegalStateException("Auto-renew is disabled");
        }
        if (status != ShopAccountStatus.EXPIRED) {
            throw new IllegalStateException("Only expired accounts can be renewed");
        }
        requireDurationKnown();
        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate(now, currentPlanDurationMonths);
        this.refundDeadline = calculateRefundDeadline(now);
        setStatus(ShopAccountStatus.ACTIVE);
    }

    /**
     * Upgrades the shop to a new plan.
     */
    public void upgrade(SubscriptionPlan newPlan) {
        ValidationUtils.validateNotBlank(newPlan, "newPlan");
        if (!isActive()) {
            throw new IllegalStateException("Shop must be active to upgrade");
        }
        if (newPlan.getPayerType() != PayerType.SHOP) {
            throw new IllegalArgumentException("Plan payerType must be SHOP for a ShopAccount");
        }
        this.planId = newPlan.getPlanId();
        this.currentPlanDurationMonths = newPlan.getDurationMonths();
        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = calculateEndDate(now, currentPlanDurationMonths);
        this.refundDeadline = calculateRefundDeadline(now);
        setStatus(ShopAccountStatus.ACTIVE);
    }

    /**
     * Extends the current plan by its duration.
     */
    public void extendPlan() {
        requireActive();
        requireDurationKnown();
        ValidationUtils.validateNotBlank(endDate, "endDate");
        this.endDate = endDate.plusMonths(currentPlanDurationMonths);
    }

    /**
     * Cron/job helper: checks expiration and returns current status.
     */
    public ShopAccountStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    // --- Setters ---

    public void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    // --- Guards ---

    private void setStatus(ShopAccountStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void requireActive() {
        if (status != ShopAccountStatus.ACTIVE) {
            throw new IllegalStateException("Account must be ACTIVE");
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
        if (!(o instanceof ShopAccount)) return false;
        ShopAccount that = (ShopAccount) o;
        return subscriptionId != null && subscriptionId.equals(that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return subscriptionId != null ? subscriptionId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ShopAccount{" +
                "subscriptionId=" + subscriptionId +
                ", shopId=" + shopId +
                ", planId=" + planId +
                ", registryId=" + registryId +
                ", status=" + status +
                '}';
    }
}

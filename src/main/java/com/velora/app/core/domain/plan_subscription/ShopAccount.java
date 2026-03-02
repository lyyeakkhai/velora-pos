package com.velora.app.core.domain.plan_subscription;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a shop's subscription account lifecycle.
 */
public class ShopAccount {

    private UUID subscriptionId;
    private UUID shopId;
    private UUID planId;
    private UUID registryId;
    private ShopAccountStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime refundDeadline;
    private boolean autoRenew;

    private Integer currentPlanDurationMonths;

    /**
     * Creates a shop account with mandatory identifiers and initial status.
     */
    public ShopAccount(UUID shopId, UUID planId, UUID registryId, ShopAccountStatus status, boolean autoRenew) {
        setSubscriptionId(UUID.randomUUID());
        setShopId(shopId);
        setPlanId(planId);
        setRegistryId(registryId);
        setStatus(status);
        setStartDate(null);
        setEndDate(null);
        setRefundDeadline(null);
        setAutoRenew(autoRenew);
        setCurrentPlanDurationMonths(null);
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public UUID getRegistryId() {
        return registryId;
    }

    public ShopAccountStatus getStatus() {
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

    public boolean isAutoRenew() {
        return autoRenew;
    }

    /**
     * Activates a plan for this shop (sets timestamps).
     */
    public void activatePlan(SubscriptionPlan plan) {
        ValidationUtils.validateNotBlank(plan, "plan");
        if (!plan.isAvailable()) {
            throw new IllegalStateException("Plan is not available");
        }
        if (plan.getPayerType() != PayerType.SHOP) {
            throw new IllegalArgumentException("Plan payerType must be SHOP for a ShopAccount");
        }
        setPlanId(plan.getPlanId());
        setCurrentPlanDurationMonths(plan.getDurationMonths());
        LocalDateTime now = LocalDateTime.now();
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());
        setStatus(ShopAccountStatus.ACTIVE);
    }

    /**
     * Extends the current plan by its duration.
     */
    public void extendPlan() {
        requireActive();
        requireDurationKnown();
        ValidationUtils.validateNotBlank(endDate, "endDate");
        setEndDate(endDate.plusMonths(currentPlanDurationMonths));
    }

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
     * Cancels the shop account immediately.
     */
    public void cancel() {
        if (status == ShopAccountStatus.CANCELLED) {
            throw new IllegalStateException("Account already cancelled");
        }
        setStatus(ShopAccountStatus.CANCELLED);
        if (startDate != null) {
            setEndDate(LocalDateTime.now());
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
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());
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
        setPlanId(newPlan.getPlanId());
        setCurrentPlanDurationMonths(newPlan.getDurationMonths());
        LocalDateTime now = LocalDateTime.now();
        setStartDate(now);
        setEndDate(calculateEndDate());
        setRefundDeadline(calculateRefundDeadline());
        setStatus(ShopAccountStatus.ACTIVE);
    }

    /**
     * Returns true if active and not expired.
     */
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
     * Cron/job helper: marks expired if needed.
     */
    public void markExpiredIfNeeded() {
        if (status == ShopAccountStatus.ACTIVE
                && endDate != null
                && !endDate.isAfter(LocalDateTime.now())) {
            setStatus(ShopAccountStatus.EXPIRED);
        }
    }

    /**
     * Cron/job helper: checks expiration and returns current status.
     */
    public ShopAccountStatus checkExpiration() {
        markExpiredIfNeeded();
        return status;
    }

    public LocalDateTime calculateEndDate() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        requireDurationKnown();
        return startDate.plusMonths(currentPlanDurationMonths);
    }

    public LocalDateTime calculateRefundDeadline() {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        return startDate.plusDays(14);
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

    private void setSubscriptionId(UUID subscriptionId) {
        ValidationUtils.validateUUID(subscriptionId, "subscriptionId");
        this.subscriptionId = subscriptionId;
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    public void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    private void setRegistryId(UUID registryId) {
        ValidationUtils.validateUUID(registryId, "registryId");
        this.registryId = registryId;
    }

    private void setStatus(ShopAccountStatus status) {
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

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    private void setCurrentPlanDurationMonths(Integer currentPlanDurationMonths) {
        this.currentPlanDurationMonths = currentPlanDurationMonths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShopAccount)) {
            return false;
        }
        ShopAccount that = (ShopAccount) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
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

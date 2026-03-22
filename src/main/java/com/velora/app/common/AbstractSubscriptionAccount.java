package com.velora.app.common;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.core.domain.plan_subscription.SubscriptionPlan;

/**
 * Abstract base class for subscription account lifecycle management.
 *
 * <p>Shared by {@code UserAccount} and {@code ShopAccount}. Provides common fields
 * and concrete helper methods for date calculation and expiration checking.
 * Subclasses must implement {@link #isActive()}, {@link #activatePlan(SubscriptionPlan)},
 * and {@link #cancel()} with their own domain-specific logic.
 *
 * <p>Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7
 */
public abstract class AbstractSubscriptionAccount {

    protected UUID subscriptionId;
    protected UUID planId;
    protected UUID registryId;
    protected LocalDateTime startDate;
    protected LocalDateTime endDate;
    protected LocalDateTime refundDeadline;
    protected Integer currentPlanDurationMonths;

    /**
     * Calculates the end date by adding {@code months} to {@code start}.
     *
     * @param start  the subscription start date/time
     * @param months number of months to add (must be positive)
     * @return the computed end date/time
     * @throws DomainException if {@code start} is null or {@code months} is not positive
     */
    protected LocalDateTime calculateEndDate(LocalDateTime start, int months) {
        if (start == null) throw new DomainException("startDate must not be null");
        if (months <= 0) throw new DomainException("months must be positive");
        return start.plusMonths(months);
    }

    /**
     * Calculates the refund deadline as {@code start} + 14 days.
     *
     * @param start the subscription start date/time
     * @return the refund deadline (14 days after start)
     * @throws DomainException if {@code start} is null
     */
    protected LocalDateTime calculateRefundDeadline(LocalDateTime start) {
        if (start == null) throw new DomainException("startDate must not be null");
        return start.plusDays(14);
    }

    /**
     * Marks the account as expired if the end date has passed and the account is still active.
     * Delegates the active check to {@link #isActive()} and the status transition to
     * {@link #expireStatus()}, which subclasses must implement.
     *
     * <p>Requirement: 5.4
     */
    public void markExpiredIfNeeded() {
        if (isActive() && endDate != null && !endDate.isAfter(LocalDateTime.now())) {
            expireStatus();
        }
    }

    /**
     * Transitions the account status to EXPIRED. Called by {@link #markExpiredIfNeeded()}.
     * Subclasses set their own status enum value here.
     */
    protected abstract void expireStatus();

    /**
     * Returns true if this account is currently active.
     *
     * <p>Requirement: 5.5
     */
    public abstract boolean isActive();

    /**
     * Activates the given plan on this account, setting start/end dates and refund deadline.
     *
     * <p>Requirement: 5.6
     *
     * @param plan the plan to activate
     */
    public abstract void activatePlan(SubscriptionPlan plan);

    /**
     * Cancels this subscription account.
     *
     * <p>Requirement: 5.7
     */
    public abstract void cancel();

    // --- Getters ---

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public UUID getRegistryId() {
        return registryId;
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

    public Integer getCurrentPlanDurationMonths() {
        return currentPlanDurationMonths;
    }
}

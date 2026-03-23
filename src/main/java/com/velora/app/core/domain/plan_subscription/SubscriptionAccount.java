package com.velora.app.core.domain.plan_subscription;

import java.util.UUID;

/**
 * Unified interface for subscription account lifecycle operations.
 *
 * <p>Implemented by both {@link UserAccount} and {@link ShopAccount}, allowing
 * the subscription activation router and service layer to operate on either
 * account type polymorphically without knowing the concrete type.
 *
 * <p>Requirements: 19.1, 19.2, 19.3
 */
public interface SubscriptionAccount {

    UUID getSubscriptionId();

    UUID getPlanId();

    UUID getRegistryId();

    /**
     * Activates the given plan on this account.
     */
    void activatePlan(SubscriptionPlan plan);

    /**
     * Expires this account immediately.
     */
    void expire();

    /**
     * Cancels this account.
     */
    void cancel();

    /**
     * Returns true if this account is currently active.
     */
    boolean isActive();

    /**
     * Transitions to EXPIRED if the end date has passed and the account is still active.
     */
    void markExpiredIfNeeded();
}

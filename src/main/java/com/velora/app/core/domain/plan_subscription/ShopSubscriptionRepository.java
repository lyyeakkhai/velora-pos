package com.velora.app.core.domain.plan_subscription;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository interface for ShopSubscription persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * Requirements: 14.2
 */
public interface ShopSubscriptionRepository {

    /**
     * Persists a shop subscription record and returns the saved instance.
     *
     * @param subscription The subscription to save (cannot be null)
     * @return The saved subscription
     */
    ShopSubscription save(ShopSubscription subscription);

    /**
     * Returns all subscription records for a given shop.
     *
     * @param shopId The shop UUID
     * @return A list of subscription records for that shop (may be empty)
     */
    List<ShopSubscription> findByShopId(UUID shopId);
}

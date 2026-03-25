package com.velora.app.modules.plan_subscriptionModule.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for ShopAccount persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * Requirements: 14.2
 */
public interface ShopAccountRepository {

    /**
     * Persists a shop account and returns the saved instance.
     *
     * @param account The account to save (cannot be null)
     * @return The saved account
     */
    ShopAccount save(ShopAccount account);

    /**
     * Finds the subscription account for a given shop.
     *
     * @param shopId The shop UUID
     * @return An Optional containing the account, or empty if not found
     */
    Optional<ShopAccount> findByShopId(UUID shopId);

    /**
     * Returns all shop accounts with an ACTIVE status.
     *
     * @return A list of active shop accounts (may be empty)
     */
    List<ShopAccount> findAllActive();
}

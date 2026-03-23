package com.velora.app.core.domain.plan_subscription;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository interface for UserSubscription persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * Requirements: 14.2
 */
public interface UserSubscriptionRepository {

    /**
     * Persists a user subscription record and returns the saved instance.
     *
     * @param subscription The subscription to save (cannot be null)
     * @return The saved subscription
     */
    UserSubscription save(UserSubscription subscription);

    /**
     * Returns all subscription records for a given user.
     *
     * @param userId The user UUID
     * @return A list of subscription records for that user (may be empty)
     */
    List<UserSubscription> findByUserId(UUID userId);
}

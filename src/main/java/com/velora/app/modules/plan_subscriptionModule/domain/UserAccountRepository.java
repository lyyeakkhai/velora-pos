package com.velora.app.modules.plan_subscriptionModule.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for UserAccount persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * Requirements: 14.2
 */
public interface UserAccountRepository {

    /**
     * Persists a user account and returns the saved instance.
     *
     * @param account The account to save (cannot be null)
     * @return The saved account
     */
    UserAccount save(UserAccount account);

    /**
     * Finds the subscription account for a given user.
     *
     * @param userId The user UUID
     * @return An Optional containing the account, or empty if not found
     */
    Optional<UserAccount> findByUserId(UUID userId);

    /**
     * Returns all user accounts with an ACTIVE status.
     *
     * @return A list of active user accounts (may be empty)
     */
    List<UserAccount> findAllActive();
}

package com.velora.app.modules.plan_subscriptionModule.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for SubscriptionPlan persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * Requirements: 14.2
 */
public interface SubscriptionPlanRepository {

    /**
     * Persists a subscription plan and returns the saved instance.
     *
     * @param plan The plan to save (cannot be null)
     * @return The saved plan
     */
    SubscriptionPlan save(SubscriptionPlan plan);

    /**
     * Finds a subscription plan by its unique identifier.
     *
     * @param planId The plan UUID
     * @return An Optional containing the plan, or empty if not found
     */
    Optional<SubscriptionPlan> findById(UUID planId);

    /**
     * Finds a subscription plan by its URL-friendly slug.
     *
     * @param slug The plan slug
     * @return An Optional containing the plan, or empty if not found
     */
    Optional<SubscriptionPlan> findBySlug(String slug);

    /**
     * Returns all currently active subscription plans.
     *
     * @return A list of active plans (may be empty)
     */
    List<SubscriptionPlan> findAllActive();
}

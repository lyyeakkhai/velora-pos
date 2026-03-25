package com.velora.app.modules.plan_subscriptionModule.service;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.plan_subscriptionModule.domain.PlanSubscriptionEngine;

import java.util.UUID;

/**
 * Application-layer contract for subscription lifecycle management.
 *
 * <p>Requirement: 16.1, 16.3
 */
public interface ISubscriptionService {

    /**
     * Onboards a new user: creates PlatformRegistry, UserAccount, and activates the basic plan.
     */
    PlanSubscriptionEngine.UserOnboardingResult onboardUser(UUID userId, UUID basicPlanId);

    /**
     * Activates a paid plan for a user account.
     */
    void activateUserPlan(UUID userId, UUID planId, UUID transactionId);

    /**
     * Activates a paid plan for a shop account.
     */
    void activateShopPlan(UUID shopId, UUID planId, UUID transactionId);

    /**
     * Upgrades a user account to a new plan.
     */
    void upgradeUserPlan(UUID userId, UUID newPlanId);

    /**
     * Cancels a user's active subscription.
     */
    void cancelUserSubscription(UUID userId);

    /**
     * Batch job: marks all expired accounts as EXPIRED.
     */
    void runExpirationJob();

    /**
     * Returns true if the user's current plan includes the given feature key.
     */
    boolean userHasFeature(UUID userId, String featureKey);

    /**
     * Bans a platform registry. Requires SUPER_ADMIN.
     */
    void banRegistry(UUID actorId, UUID registryId, String reason);
}

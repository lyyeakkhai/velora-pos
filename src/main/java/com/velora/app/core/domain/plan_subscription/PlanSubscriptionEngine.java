package com.velora.app.core.domain.plan_subscription;

import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain-level orchestration for onboarding, shop creation, and payment
 * confirmation.
 * <p>
 * No persistence and no external calls; pure business logic.
 */
public final class PlanSubscriptionEngine {

    private PlanSubscriptionEngine() {
        // utility
    }

    public record UserOnboardingResult(PlatformRegistry registry, UserAccount account) {
    }

    public record ShopCreationResult(PlatformRegistry registry, ShopAccount account) {
    }

    /**
     * User onboarding: creates registry + user account and assigns a basic plan.
     */
    public static UserOnboardingResult onboardUser(UUID userId, SubscriptionPlan basicPlan) {
        ValidationUtils.validateUUID(userId, "userId");
        ValidationUtils.validateNotBlank(basicPlan, "basicPlan");
        if (basicPlan.getPayerType() != PayerType.USER) {
            throw new IllegalArgumentException("basicPlan payerType must be USER");
        }

        PlatformRegistry registry = new PlatformRegistry(userId, TargetType.USER, RegistryStatus.ACTIVE);
        UserAccount account = new UserAccount(userId, basicPlan.getPlanId(), registry.getRegistryId(),
                UserAccountStatus.TRIAL);
        account.activatePlan(basicPlan);
        return new UserOnboardingResult(registry, account);
    }

    /**
     * Shop creation: requires active user account. Registry remains PENDING until
     * payment is confirmed.
     */
    public static ShopCreationResult createShop(UUID shopId, UserAccount ownerAccount, SubscriptionPlan shopPlan,
            boolean autoRenew) {
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(ownerAccount, "ownerAccount");
        ValidationUtils.validateNotBlank(shopPlan, "shopPlan");

        if (!ownerAccount.isActive()) {
            throw new IllegalStateException("Owner must have an active UserAccount");
        }
        if (shopPlan.getPayerType() != PayerType.SHOP) {
            throw new IllegalArgumentException("shopPlan payerType must be SHOP");
        }

        PlatformRegistry registry = new PlatformRegistry(shopId, TargetType.SHOP, RegistryStatus.PENDING);
        ShopAccount account = new ShopAccount(shopId, shopPlan.getPlanId(), registry.getRegistryId(),
                ShopAccountStatus.PAST_DUE, autoRenew);
        return new ShopCreationResult(registry, account);
    }

    /**
     * Payment confirmation: success activates plan; failed keeps registry locked.
     *
     * Extends existing active subscriptions when present.
     */
    public static void confirmShopPayment(boolean success, UUID transactionId, PlatformRegistry registry,
            ShopAccount shopAccount, SubscriptionPlan plan) {
        ValidationUtils.validateNotBlank(registry, "registry");
        ValidationUtils.validateNotBlank(shopAccount, "shopAccount");
        ValidationUtils.validateNotBlank(plan, "plan");
        if (plan.getPayerType() != PayerType.SHOP) {
            throw new IllegalArgumentException("plan payerType must be SHOP");
        }
        if (!Objects.equals(shopAccount.getRegistryId(), registry.getRegistryId())) {
            throw new IllegalArgumentException("shopAccount.registryId must match registry.registryId");
        }

        if (!success) {
            return;
        }

        registry.setTransactionId(transactionId);
        if (registry.getStatus() != RegistryStatus.ACTIVE) {
            registry.activate();
        }

        if (shopAccount.isActive()) {
            shopAccount.extendPlan();
        } else {
            shopAccount.activatePlan(plan);
        }
    }
}

package com.velora.app.core.domain.plan_subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Test;

public class PlanSubscriptionTesting {

    @Test(expected = IllegalArgumentException.class)
    public void subscriptionPlan_invalidSlug_throws() {
        new SubscriptionPlan("Pro", "PRO-PLAN", new BigDecimal("10"), 1, PayerType.USER, true);
    }

    @Test
    public void subscriptionPlan_featureAccess_enabledAndLimit() {
        SubscriptionPlan plan = new SubscriptionPlan("Pro", "pro", new BigDecimal("10"), 1, PayerType.USER, true);
        Feature feature = new Feature("reports", TargetType.USER, "Access reports");

        plan.upsertFeature(feature, 100, true);
        assertTrue(plan.hasFeature("reports"));
        assertEquals(Integer.valueOf(100), plan.getFeatureLimit("reports"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void feature_invalidKey_throws() {
        new Feature("invalid-key", TargetType.USER, "x");
    }

    @Test(expected = IllegalStateException.class)
    public void registry_bannedCannotActivate() {
        PlatformRegistry registry = new PlatformRegistry(UUID.randomUUID(), TargetType.USER, RegistryStatus.PENDING);
        registry.ban("fraud");
        registry.activate();
    }

    @Test
    public void userAccount_activatePlan_setsDatesAndStatus() {
        SubscriptionPlan plan = new SubscriptionPlan("Basic", "basic", new BigDecimal("0"), 1, PayerType.USER, true);
        PlatformRegistry registry = new PlatformRegistry(UUID.randomUUID(), TargetType.USER, RegistryStatus.ACTIVE);
        UserAccount account = new UserAccount(UUID.randomUUID(), plan.getPlanId(), registry.getRegistryId(),
                UserAccountStatus.TRIAL);

        account.activatePlan(plan);
        assertNotNull(account.getStartDate());
        assertNotNull(account.getEndDate());
        assertNotNull(account.getRefundDeadline());
        assertEquals(UserAccountStatus.TRIAL, account.getStatus());
        assertTrue(account.getEndDate().isAfter(account.getStartDate()));
    }

    @Test(expected = IllegalStateException.class)
    public void shopCreation_requiresActiveOwner() {
        SubscriptionPlan userPlan = new SubscriptionPlan("Basic", "basic", new BigDecimal("0"), 1, PayerType.USER,
                true);
        PlatformRegistry registry = new PlatformRegistry(UUID.randomUUID(), TargetType.USER, RegistryStatus.ACTIVE);
        UserAccount owner = new UserAccount(UUID.randomUUID(), userPlan.getPlanId(), registry.getRegistryId(),
                UserAccountStatus.TRIAL);
        // not activated => not active

        SubscriptionPlan shopPlan = new SubscriptionPlan("Shop", "shop", new BigDecimal("25"), 1, PayerType.SHOP,
                true);
        PlanSubscriptionEngine.createShop(UUID.randomUUID(), owner, shopPlan, true);
    }

    @Test
    public void paymentConfirmation_success_activatesRegistryAndAccount() {
        SubscriptionPlan userPlan = new SubscriptionPlan("Basic", "basic", new BigDecimal("0"), 1, PayerType.USER,
                true);
        PlanSubscriptionEngine.UserOnboardingResult onboard = PlanSubscriptionEngine.onboardUser(UUID.randomUUID(),
                userPlan);

        SubscriptionPlan shopPlan = new SubscriptionPlan("Shop", "shop", new BigDecimal("25"), 1, PayerType.SHOP,
                true);
        PlanSubscriptionEngine.ShopCreationResult created = PlanSubscriptionEngine.createShop(UUID.randomUUID(),
                onboard.account(), shopPlan, true);

        assertEquals(RegistryStatus.PENDING, created.registry().getStatus());
        assertEquals(ShopAccountStatus.PAST_DUE, created.account().getStatus());

        PlanSubscriptionEngine.confirmShopPayment(true, UUID.randomUUID(), created.registry(), created.account(),
                shopPlan);

        assertEquals(RegistryStatus.ACTIVE, created.registry().getStatus());
        assertEquals(ShopAccountStatus.ACTIVE, created.account().getStatus());
        assertTrue(created.account().isActive());
    }

    @Test(expected = IllegalStateException.class)
    public void userSubscription_markRefundedTwice_throws() {
        SubscriptionPlan plan = new SubscriptionPlan("Pro", "pro", new BigDecimal("10"), 1, PayerType.USER, true);
        UserSubscription sub = new UserSubscription(UUID.randomUUID(), UUID.randomUUID(), plan);

        sub.markRefunded();
        sub.markRefunded();
    }

    @Test
    public void shopSubscription_endDateAndDeadline_computed() {
        SubscriptionPlan plan = new SubscriptionPlan("Shop", "shop", new BigDecimal("25"), 2, PayerType.SHOP, true);
        ShopSubscription sub = new ShopSubscription(UUID.randomUUID(), UUID.randomUUID(), plan);

        assertNotNull(sub.getStartDate());
        assertNotNull(sub.getEndDate());
        assertNotNull(sub.getRefundDeadline());
        assertTrue(sub.getEndDate().isAfter(sub.getStartDate()));
        assertTrue(sub.getRefundDeadline().isAfter(sub.getStartDate()));
    }
}

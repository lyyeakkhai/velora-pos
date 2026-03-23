package com.velora.app.core.service.subscription;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Membership;
import com.velora.app.core.domain.auth.MembershipRepository;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.plan_subscription.PlanSubscriptionEngine;
import com.velora.app.core.domain.plan_subscription.PlatformRegistry;
import com.velora.app.core.domain.plan_subscription.PlatformRegistryRepository;
import com.velora.app.core.domain.plan_subscription.ShopAccount;
import com.velora.app.core.domain.plan_subscription.ShopAccountRepository;
import com.velora.app.core.domain.plan_subscription.SubscriptionPlan;
import com.velora.app.core.domain.plan_subscription.SubscriptionPlanRepository;
import com.velora.app.core.domain.plan_subscription.UserAccount;
import com.velora.app.core.domain.plan_subscription.UserAccountRepository;
import com.velora.app.core.service.ISubscriptionService;

import java.util.List;
import java.util.UUID;

/**
 * Application-layer service for subscription lifecycle management.
 *
 * <p>Extends {@link AbstractDomainService} to reuse {@code requireRole} and
 * {@code requireNotNull} guard methods.
 *
 * <p>Requirements: 16.1, 16.3
 */
public class SubscriptionService extends AbstractDomainService implements ISubscriptionService {

    private final UserAccountRepository userAccountRepository;
    private final ShopAccountRepository shopAccountRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlatformRegistryRepository platformRegistryRepository;
    private final MembershipRepository membershipRepository;

    public SubscriptionService(
            UserAccountRepository userAccountRepository,
            ShopAccountRepository shopAccountRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            PlatformRegistryRepository platformRegistryRepository,
            MembershipRepository membershipRepository) {
        this.userAccountRepository = userAccountRepository;
        this.shopAccountRepository = shopAccountRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.platformRegistryRepository = platformRegistryRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * Onboards a new user: creates PlatformRegistry, UserAccount, and activates the basic plan.
     *
     * <p>Delegates to {@link PlanSubscriptionEngine#onboardUser} for pure domain logic,
     * then persists both the registry and account.
     *
     * @param userId      the user's UUID
     * @param basicPlanId the UUID of the free/basic plan to activate
     * @return the onboarding result containing the new registry and account
     * @throws DomainException if the plan is not found
     */
    @Override
    public PlanSubscriptionEngine.UserOnboardingResult onboardUser(UUID userId, UUID basicPlanId) {
        requireNotNull(userId, "userId");
        requireNotNull(basicPlanId, "basicPlanId");

        SubscriptionPlan basicPlan = subscriptionPlanRepository.findById(basicPlanId)
                .orElseThrow(() -> new DomainException("Subscription plan not found: " + basicPlanId));

        PlanSubscriptionEngine.UserOnboardingResult result = PlanSubscriptionEngine.onboardUser(userId, basicPlan);

        platformRegistryRepository.save(result.registry());
        userAccountRepository.save(result.account());

        return result;
    }

    /**
     * Activates a paid plan for a user account.
     *
     * @param userId        the user's UUID
     * @param planId        the UUID of the plan to activate
     * @param transactionId the payment transaction UUID
     * @throws DomainException if the account or plan is not found
     */
    @Override
    public void activateUserPlan(UUID userId, UUID planId, UUID transactionId) {
        requireNotNull(userId, "userId");
        requireNotNull(planId, "planId");
        requireNotNull(transactionId, "transactionId");

        UserAccount account = userAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("UserAccount not found for user: " + userId));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new DomainException("Subscription plan not found: " + planId));

        account.setTransactionId(transactionId);
        account.activatePlan(plan);
        userAccountRepository.save(account);
    }

    /**
     * Activates a paid plan for a shop account.
     *
     * @param shopId        the shop's UUID
     * @param planId        the UUID of the plan to activate
     * @param transactionId the payment transaction UUID
     * @throws DomainException if the account or plan is not found
     */
    @Override
    public void activateShopPlan(UUID shopId, UUID planId, UUID transactionId) {
        requireNotNull(shopId, "shopId");
        requireNotNull(planId, "planId");
        requireNotNull(transactionId, "transactionId");

        ShopAccount account = shopAccountRepository.findByShopId(shopId)
                .orElseThrow(() -> new DomainException("ShopAccount not found for shop: " + shopId));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new DomainException("Subscription plan not found: " + planId));

        account.activatePlan(plan);
        shopAccountRepository.save(account);
    }

    /**
     * Upgrades a user account to a new plan.
     *
     * @param userId    the user's UUID
     * @param newPlanId the UUID of the new plan
     * @throws DomainException if the account or plan is not found, or the account is not active
     */
    @Override
    public void upgradeUserPlan(UUID userId, UUID newPlanId) {
        requireNotNull(userId, "userId");
        requireNotNull(newPlanId, "newPlanId");

        UserAccount account = userAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("UserAccount not found for user: " + userId));

        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(newPlanId)
                .orElseThrow(() -> new DomainException("Subscription plan not found: " + newPlanId));

        account.upgrade(newPlan);
        userAccountRepository.save(account);
    }

    /**
     * Cancels a user's active subscription.
     *
     * @param userId the user's UUID
     * @throws DomainException if the account is not found or already cancelled
     */
    @Override
    public void cancelUserSubscription(UUID userId) {
        requireNotNull(userId, "userId");

        UserAccount account = userAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("UserAccount not found for user: " + userId));

        account.cancel();
        userAccountRepository.save(account);
    }

    /**
     * Batch job: iterates all active user and shop accounts and marks expired ones.
     *
     * <p>Calls {@link UserAccount#markExpiredIfNeeded()} and
     * {@link ShopAccount#markExpiredIfNeeded()} on each account, then persists
     * any that transitioned to EXPIRED.
     */
    @Override
    public void runExpirationJob() {
        List<UserAccount> activeUserAccounts = userAccountRepository.findAllActive();
        for (UserAccount account : activeUserAccounts) {
            boolean wasBefore = account.isActive();
            account.markExpiredIfNeeded();
            if (wasBefore && !account.isActive()) {
                userAccountRepository.save(account);
            }
        }

        List<ShopAccount> activeShopAccounts = shopAccountRepository.findAllActive();
        for (ShopAccount account : activeShopAccounts) {
            boolean wasBefore = account.isActive();
            account.markExpiredIfNeeded();
            if (wasBefore && !account.isActive()) {
                shopAccountRepository.save(account);
            }
        }
    }

    /**
     * Returns true if the user's current plan includes the given feature key.
     *
     * @param userId     the user's UUID
     * @param featureKey the feature key to check
     * @return true if the plan has the feature enabled
     * @throws DomainException if the account or plan is not found
     */
    @Override
    public boolean userHasFeature(UUID userId, String featureKey) {
        requireNotNull(userId, "userId");
        requireNotNull(featureKey, "featureKey");

        UserAccount account = userAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("UserAccount not found for user: " + userId));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(account.getPlanId())
                .orElseThrow(() -> new DomainException("Subscription plan not found: " + account.getPlanId()));

        return plan.hasFeature(featureKey);
    }

    /**
     * Bans a platform registry. Requires the actor to hold the SUPER_ADMIN role.
     *
     * @param actorId    the UUID of the admin performing the action
     * @param registryId the UUID of the registry to ban
     * @param reason     the reason for the ban
     * @throws DomainException if the actor is not SUPER_ADMIN or the registry is not found
     */
    @Override
    public void banRegistry(UUID actorId, UUID registryId, String reason) {
        requireNotNull(actorId, "actorId");
        requireNotNull(registryId, "registryId");
        requireNotNull(reason, "reason");

        Role.RoleName actorRole = resolveActorRole(actorId);
        requireRole(actorRole, Role.RoleName.SUPER_ADMIN);

        PlatformRegistry registry = platformRegistryRepository.findById(registryId)
                .orElseThrow(() -> new DomainException("PlatformRegistry not found: " + registryId));

        registry.ban(reason);
        platformRegistryRepository.save(registry);
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Resolves the highest-privilege role for the given actor from their memberships.
     * Returns {@code SELLER} as the default when no membership exists.
     */
    private Role.RoleName resolveActorRole(UUID actorId) {
        return membershipRepository.findByUserId(actorId).stream()
                .map(m -> resolveRoleNameFromId(m.getRoleId()))
                .reduce(Role.RoleName.SELLER, SubscriptionService::highestPrivilege);
    }

    /**
     * Placeholder — returns SELLER until a RoleRepository is wired in.
     */
    private Role.RoleName resolveRoleNameFromId(UUID roleId) {
        // TODO: inject RoleRepository and look up by roleId
        return Role.RoleName.SELLER;
    }

    private static Role.RoleName highestPrivilege(Role.RoleName a, Role.RoleName b) {
        return ordinal(a) <= ordinal(b) ? a : b;
    }

    private static int ordinal(Role.RoleName r) {
        return switch (r) {
            case SUPER_ADMIN -> 0;
            case OWNER       -> 1;
            case MANAGER     -> 2;
            case SELLER      -> 3;
        };
    }
}

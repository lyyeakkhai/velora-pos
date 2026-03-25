package com.velora.app.modules.plan_subscriptionModule.domain;

import com.velora.app.common.DomainException;

/**
 * Routes subscription activation to the correct {@link SubscriptionAccount}
 * based on the given {@link TargetType}.
 *
 * <p>Eliminates branching in the service layer — callers receive a
 * {@link SubscriptionAccount} and operate on it uniformly.
 *
 * <p>Requirements: 19.4, 19.5
 */
public final class SubscriptionActivationRouter {

    private SubscriptionActivationRouter() {
        // utility
    }

    /**
     * Returns the appropriate {@link SubscriptionAccount} for the given target type.
     *
     * @param targetType  USER or SHOP
     * @param userAccount the user account (used when targetType is USER)
     * @param shopAccount the shop account (used when targetType is SHOP)
     * @return the matching SubscriptionAccount
     * @throws DomainException if targetType is null, BOTH, or unrecognised
     */
    public static SubscriptionAccount route(TargetType targetType,
                                            UserAccount userAccount,
                                            ShopAccount shopAccount) {
        if (targetType == null) {
            throw new DomainException("targetType must not be null");
        }
        switch (targetType) {
            case USER:
                if (userAccount == null) {
                    throw new DomainException("userAccount must not be null for targetType USER");
                }
                return userAccount;
            case SHOP:
                if (shopAccount == null) {
                    throw new DomainException("shopAccount must not be null for targetType SHOP");
                }
                return shopAccount;
            default:
                throw new DomainException("Unknown or unsupported targetType: " + targetType);
        }
    }
}

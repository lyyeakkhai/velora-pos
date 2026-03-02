package com.velora.app.core.domain.reportandanalytic;

import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Centralized role enforcement for analytics/report access.
 */
public final class AnalyticsAccessPolicy {

    private AnalyticsAccessPolicy() {
        // utility
    }

    public static void requireOwner(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName != Role.RoleName.OWNER && roleName != Role.RoleName.SUPER_ADMIN) {
            throw new IllegalStateException("OWNER role required");
        }
    }

    public static void requireManagerOrOwner(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName == Role.RoleName.SELLER) {
            throw new IllegalStateException("MANAGER role required");
        }
    }

    public static void requireSellerSelfOrElevated(Role.RoleName roleName, UUID actorSellerId, UUID requestedSellerId) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        ValidationUtils.validateUUID(requestedSellerId, "requestedSellerId");
        if (roleName == Role.RoleName.SELLER) {
            ValidationUtils.validateUUID(actorSellerId, "actorSellerId");
            if (!actorSellerId.equals(requestedSellerId)) {
                throw new IllegalStateException("SELLER can access personal performance only");
            }
        }
    }
}

package com.velora.app.core.domain.reportandanalytic;

import com.velora.app.common.AbstractAccessPolicy;
import com.velora.app.common.AccessPolicy;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Access-control rules for analytics/reports.
 * Extends AbstractAccessPolicy; implements check() with analytics-specific role rules.
 * Requirements: 9.6
 */
public class AnalyticsAccessPolicy extends AbstractAccessPolicy implements AccessPolicy {

    @Override
    public void check(Role.RoleName actorRole, String operation) {
        if (actorRole == Role.RoleName.SUPER_ADMIN) return;
        switch (operation) {
            case "ADMIN_ONLY":
                throw new DomainException("SUPER_ADMIN role required for: " + operation);
            case "REQUIRE_OWNER":
                if (actorRole != Role.RoleName.OWNER) throw new DomainException("OWNER role required");
                break;
            case "REQUIRE_MANAGER_OR_OWNER":
                if (actorRole == Role.RoleName.SELLER) throw new DomainException("MANAGER role required");
                break;
            default:
                throw new DomainException("Unknown operation: " + operation);
        }
    }

    public void requireOwner(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName != Role.RoleName.OWNER && roleName != Role.RoleName.SUPER_ADMIN) throw new DomainException("OWNER role required");
    }

    public void requireManagerOrOwner(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName == Role.RoleName.SELLER) throw new DomainException("MANAGER role required");
    }

    public void requireSellerSelfOrElevated(Role.RoleName roleName, UUID actorSellerId, UUID requestedSellerId) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        ValidationUtils.validateUUID(requestedSellerId, "requestedSellerId");
        if (roleName == Role.RoleName.SELLER) {
            ValidationUtils.validateUUID(actorSellerId, "actorSellerId");
            if (!actorSellerId.equals(requestedSellerId)) throw new DomainException("SELLER can access personal performance only");
        }
    }
}

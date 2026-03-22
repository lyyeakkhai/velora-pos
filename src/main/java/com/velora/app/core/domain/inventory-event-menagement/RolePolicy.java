package com.velora.app.core.domain.inventoryeventmanagement;

import com.velora.app.common.AbstractAccessPolicy;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Centralized role enforcement for inventory/event management.
 * Extends AbstractAccessPolicy; implements check() with inventory-specific role rules.
 * Requirements: 9.5
 */
public class RolePolicy extends AbstractAccessPolicy {

    @Override
    public void check(Role.RoleName actorRole, String operation) {
        if (actorRole == Role.RoleName.SUPER_ADMIN) return;
        switch (operation) {
            case "ADMIN_ONLY":
                throw new DomainException("SUPER_ADMIN role required for: " + operation);
            case "REQUIRE_OWNER":
                if (actorRole != Role.RoleName.OWNER) throw new DomainException("OWNER role required");
                break;
            case "CATALOG_WRITE":
                if (actorRole == Role.RoleName.SELLER) throw new DomainException("Write access denied for SELLER");
                break;
            default:
                throw new DomainException("Unknown operation: " + operation);
        }
    }

    public void requireOwner(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName != Role.RoleName.OWNER && roleName != Role.RoleName.SUPER_ADMIN) throw new DomainException("OWNER role required");
    }

    public void requireCatalogWrite(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName == Role.RoleName.SELLER) throw new DomainException("Write access denied for SELLER");
    }
}

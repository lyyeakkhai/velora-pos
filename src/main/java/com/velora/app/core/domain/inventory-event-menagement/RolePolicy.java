package com.velora.app.core.domain.inventoryeventmanagement;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Centralized role enforcement helpers.
 */
public final class RolePolicy {

    private RolePolicy() {
        // utility
    }

    public static void requireOwner(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName != Role.RoleName.OWNER && roleName != Role.RoleName.SUPER_ADMIN) {
            throw new IllegalStateException("OWNER role required");
        }
    }

    public static void requireCatalogWrite(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName == Role.RoleName.SELLER) {
            throw new IllegalStateException("Write access denied for SELLER");
        }
    }
}

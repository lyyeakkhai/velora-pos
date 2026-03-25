package com.velora.app.modules.authModule.domain;

import com.velora.app.common.AbstractEntity;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Represents a system role for permission classification.
 *
 * Extends AbstractEntity to inherit UUID-based identity, equals, and hashCode.
 * The roleId is used as the entity id.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public class Role extends AbstractEntity {

    /**
     * Available role types in the system
     */
    public enum RoleName {
        SUPER_ADMIN, // Full access across all shops and system settings
        OWNER,       // Full system access and shop ownership
        MANAGER,     // Administrative access within shop
        SELLER       // Standard point-of-sale access
    }

    private RoleName roleName;

    /**
     * Creates a new Role with validation.
     *
     * @param roleId   The unique identifier for this role (cannot be null)
     * @param roleName The name/type of this role (cannot be null)
     * @throws IllegalArgumentException if validation fails
     */
    public Role(UUID roleId, RoleName roleName) {
        super(roleId);
        setRoleName(roleName);
    }

    /**
     * Gets the unique identifier for this role.
     * Delegates to getId() inherited from AbstractEntity.
     *
     * @return The role ID
     */
    public UUID getRoleId() {
        return getId();
    }

    /**
     * Gets the name/type of this role.
     *
     * @return The role name
     */
    public RoleName getRoleName() {
        return roleName;
    }

    /**
     * Sets the role name with validation.
     *
     * @param roleName The new role name (cannot be null)
     * @throws IllegalArgumentException if roleName is null
     */
    public void setRoleName(RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "Role name");
        this.roleName = roleName;
    }

    /**
     * Checks if this role has administrative privileges.
     *
     * @return true if role is SUPER_ADMIN, OWNER, or MANAGER, false otherwise
     */
    public boolean isAdministrative() {
        return roleName == RoleName.SUPER_ADMIN || roleName == RoleName.OWNER || roleName == RoleName.MANAGER;
    }

    /**
     * Checks if this role can manage other users.
     *
     * @return true if role is OWNER, false otherwise
     */
    public boolean canManageUsers() {
        return roleName == RoleName.OWNER;
    }

    public boolean isSuperAdmin() {
        return roleName == RoleName.SUPER_ADMIN;
    }
}

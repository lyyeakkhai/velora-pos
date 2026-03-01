package com.velora.app.core.domain.auth;

import com.velora.app.core.utils.ValidationUtils;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a system role for permission classification.
 * 
 * This entity defines the available roles within the Velora system,
 * providing a foundation for role-based access control.
 * 
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */

public class Role {

    /**
     * Available role types in the system
     */
    public enum RoleName {
        SUPER_ADMIN, // Full access across all shops and system settings
        OWNER, // Full system access and shop ownership
        MANAGER, // Administrative access within shop
        SELLER // Standard point-of-sale access
    }

    private UUID roleId;
    private RoleName roleName;

    /**
     * Creates a new Role with validation.
     * 
     * @param roleId   The unique identifier for this role (cannot be null)
     * @param roleName The name/type of this role (cannot be null)
     * @throws IllegalArgumentException if validation fails
     */
    public Role(UUID roleId, RoleName roleName) {
        setRoleId(roleId);
        setRoleName(roleName);
    }

    /**
     * Gets the unique identifier for this role.
     * 
     * @return A copy of the role ID
     */
    public UUID getRoleId() {
        return roleId;
    }

    /**
     * Sets the role ID with validation.
     * 
     * @param roleId The new role ID (cannot be null)
     * @throws IllegalArgumentException if roleId is null
     */
    private void setRoleId(UUID roleId) {
        ValidationUtils.validateUUID(roleId, "Role ID");
        this.roleId = roleId;
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
     * @return true if role is OWNER or MANAGER, false otherwise
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


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Role role = (Role) obj;
        return Objects.equals(roleId, role.roleId) &&
                roleName == role.roleName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, roleName);
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName=" + roleName +
                '}';
    }
}
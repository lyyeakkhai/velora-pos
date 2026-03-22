package com.velora.app.core.domain.auth;

import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.RegexPatterns;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a user's membership in a shop with role-based access control.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity and
 * createdAt/updatedAt audit timestamps. All mutation methods call touch()
 * to keep updatedAt current.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public class Membership extends AbstractAuditableEntity {

    private String sellerName;
    private UUID userId;
    private UUID shopId;
    private UUID roleId;

    /**
     * Creates a new Membership for a normal user with a default role (no shop).
     *
     * @param memberId The unique identifier for this membership (cannot be null)
     * @param userId   The associated user ID (cannot be null)
     * @param roleId   The associated role ID (cannot be null)
     */
    public Membership(UUID memberId, UUID userId, UUID roleId) {
        super(memberId);
        setUserId(userId);
        setRoleId(roleId);
    }

    /**
     * Creates a new Membership for a seller with a seller name and shop.
     *
     * @param memberId   The unique identifier for this membership (cannot be null)
     * @param sellerName The display name for the seller
     * @param userId     The associated user ID (cannot be null)
     * @param shopId     The associated shop ID (cannot be null)
     * @param roleId     The associated role ID (cannot be null)
     */
    public Membership(UUID memberId, String sellerName, UUID userId, UUID shopId, UUID roleId) {
        super(memberId);
        setUserId(userId);
        setShopId(shopId);
        setRoleId(roleId);
        setSellerName(sellerName);
    }

    /**
     * Creates a new Membership for a shop owner.
     *
     * @param memberId The unique identifier for this membership (cannot be null)
     * @param userId   The associated user ID (cannot be null)
     * @param shopId   The associated shop ID (cannot be null)
     * @param roleId   The associated role ID (cannot be null)
     */
    public Membership(UUID memberId, UUID userId, UUID shopId, UUID roleId) {
        super(memberId);
        setUserId(userId);
        setShopId(shopId);
        setRoleId(roleId);
    }

    /**
     * Gets the seller display name.
     *
     * @return The seller name (may be null)
     */
    public String getSellerName() {
        return sellerName;
    }

    /**
     * Sets the seller name with validation and records the mutation timestamp.
     *
     * @param sellerName The new seller name
     * @throws IllegalArgumentException if seller name is invalid
     */
    public void setSellerName(String sellerName) {
        ValidationUtils.validateFormat(sellerName, RegexPatterns.USERNAME, "Seller name");
        this.sellerName = sellerName;
        touch();
    }

    /**
     * Gets the associated user ID.
     *
     * @return The user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Gets the associated shop ID.
     *
     * @return The shop ID
     */
    public UUID getShopId() {
        return shopId;
    }

    /**
     * Gets the associated role ID.
     *
     * @return The role ID
     */
    public UUID getRoleId() {
        return roleId;
    }

    /**
     * Sets the user ID with validation and records the mutation timestamp.
     *
     * @param userId The user ID (cannot be null)
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(UUID userId) {
        ValidationUtils.validateUUID(userId, "User ID");
        this.userId = userId;
        touch();
    }

    /**
     * Sets the shop ID with validation and records the mutation timestamp.
     *
     * @param shopId The shop ID (cannot be null)
     * @throws IllegalArgumentException if shopId is null
     */
    public void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "Shop ID");
        this.shopId = shopId;
        touch();
    }

    /**
     * Sets the role ID with validation and records the mutation timestamp.
     *
     * @param roleId The new role ID (cannot be null)
     * @throws IllegalArgumentException if roleId is null
     */
    public void setRoleId(UUID roleId) {
        ValidationUtils.validateUUID(roleId, "Role ID");
        this.roleId = roleId;
        touch();
    }

    /**
     * Checks if this membership has a seller name set.
     *
     * @return true if seller name is not null and not empty, false otherwise
     */
    public boolean hasSellerName() {
        return sellerName != null && !sellerName.trim().isEmpty();
    }

    public boolean isSeller() {
        return hasSellerName();
    }

    /**
     * Gets the seller name or a default display name.
     *
     * @param defaultName The default name to use if seller name is not set
     * @return The seller name or the provided default
     */
    public String getSellerNameOrDefault(String defaultName) {
        return hasSellerName() ? sellerName : defaultName;
    }
}

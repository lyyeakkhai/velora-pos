package com.velora.app.core.domain.auth;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.RegexPatterns;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a user's membership in a shop with role-based access control.
 * 
 * This entity defines the relationship between users and shops, including
 * their roles, permissions, and access management within the Velora system.
 * It serves as the foundation for staff identity and shop access control.
 * 
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public class Membership {

    private UUID memberId;
    private String sellerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID userId;
    private UUID shopId;
    private UUID roleId;

    /**
     * Creates a new Membership with validation.
     * 
     * @param memberId   The unique identifier for this membership (cannot be null)
     * @param sellerName The display name for the seller (nullable)
     * @param createdAt  The creation timestamp (cannot be null or future date)
     * @param updatedAt  The last update timestamp (cannot be before createdAt or
     *                   future date)
     * @param userId     The associated user ID (cannot be null)
     * @param shopId     The associated shop ID (cannot be null)
     * @param roleId     The associated role ID (cannot be null)
     * @throws IllegalArgumentException if validation fails
     */

    // use for normal user creation with default role
    public Membership(UUID memberId, UUID userId, UUID roleId) {
        setMemberId(memberId);
        setRoleId(roleId);
        setUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // user to create seller with seller name and default role
    public Membership(UUID memberId, String sellerName, UUID userId, UUID shopId, UUID roleId) {
        setMemberId(memberId);
        setRoleId(roleId);
        setUserId(userId);
        setSellerName(sellerName);
        setShopId(shopId);

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // user for create shop owner
    public Membership(UUID memberId, UUID userId, UUID shopId, UUID roleId) {
        setMemberId(memberId);
        setRoleId(roleId);
        setUserId(userId);
        setShopId(shopId);

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Gets the unique identifier for this membership.
     * 
     * @return The member ID
     */
    public UUID getMemberId() {
        return memberId;
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
     * Sets the seller name with validation.
     * 
     * @param sellerName The new seller name (can be null but not empty)
     * @throws IllegalArgumentException if seller name is empty
     */
    public void setSellerName(String sellerName) {
        ValidationUtils.validateFormat(sellerName, RegexPatterns.USERNAME, "Seller name");
        this.sellerName = sellerName;
        updateTimestamp();
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the last update timestamp.
     * 
     * @return The update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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
     * Sets the member ID with validation.
     * 
     * @param memberId The member ID (cannot be null)
     * @throws IllegalArgumentException if memberId is null
     */
    public void setMemberId(UUID memberId) {
        ValidationUtils.validateUUID(memberId, "Member ID");
        this.memberId = memberId;
        updateTimestamp();
    }

    /**
     * Sets the user ID with validation.
     * 
     * @param userId The user ID (cannot be null)
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(UUID userId) {
        ValidationUtils.validateUUID(userId, "User ID");
        this.userId = userId;
        updateTimestamp();
    }

    /**
     * Sets the shop ID with validation.
     * 
     * @param shopId The shop ID (cannot be null)
     * @throws IllegalArgumentException if shopId is null
     */
    public void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "Shop ID");
        this.shopId = shopId;
        updateTimestamp();
    }

    /**
     * Sets the role ID with validation and timestamp update.
     * 
     * @param roleId The new role ID (cannot be null)
     * @throws IllegalArgumentException if roleId is null
     */
    public void setRoleId(UUID roleId) {
        ValidationUtils.validateUUID(roleId, "Role ID");
        this.roleId = roleId;
        updateTimestamp();
    }

    /**
     * Updates the updatedAt timestamp to current time.
     */
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Manually updates the timestamp (useful for system operations).
     */
    public void touch() {
        updateTimestamp();
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

    /**
     * Checks if this membership was recently created (within last hour).
     * 
     * @return true if created within the last hour, false otherwise
     */
    public boolean isRecentlyCreated() {
        return createdAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Checks if this membership was recently updated (within last hour).
     * 
     * @return true if updated within the last hour, false otherwise
     */
    public boolean isRecentlyUpdated() {
        return updatedAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Checks if the membership has been modified since creation.
     * 
     * @return true if updatedAt is after createdAt, false otherwise
     */
    public boolean hasBeenModified() {
        return updatedAt.isAfter(createdAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Membership that = (Membership) obj;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(sellerName, that.sellerName) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(shopId, that.shopId) &&
                Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, sellerName, createdAt, updatedAt, userId, shopId, roleId);
    }

    @Override
    public String toString() {
        return "Membership{" +
                "memberId='" + memberId + '\'' +
                ", sellerName='" + sellerName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", userId=" + userId +
                ", shopId=" + shopId +
                ", roleId=" + roleId +
                '}';
    }
}
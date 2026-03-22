package com.velora.app.core.domain.auth;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;
import com.velora.app.core.utils.RegexPatterns;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents authentication credentials and provider management.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity and
 * createdAt/updatedAt audit timestamps. The authId is used as the entity id.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public class UserAuth extends AbstractAuditableEntity {

    /**
     * Supported authentication providers
     */
    public enum Provider {
        EMAIL,    // Email/password authentication
        GOOGLE,   // Google OAuth authentication
        FACEBOOK  // Facebook OAuth authentication
    }

    private Provider provider;
    private String providerUid;
    private String email;
    private String passwordHash;
    private LocalDateTime lastLoginAt;
    private UUID userId;

    /**
     * Creates a new UserAuth with validation.
     *
     * @param authId       The unique authentication identifier (cannot be null)
     * @param provider     The authentication provider (cannot be null)
     * @param providerUid  The provider-specific user ID (nullable for email auth)
     * @param email        The user's email address (must be valid format)
     * @param passwordHash The hashed password (must be >=60 chars if provided)
     * @param userId       The associated user ID (cannot be null)
     * @throws IllegalArgumentException if validation fails
     */
    public UserAuth(UUID authId, Provider provider, String providerUid, String email,
            String passwordHash, UUID userId) {
        super(authId);
        setUserId(userId);
        setEmail(email);
        setPasswordHash(passwordHash);
        setProvider(provider);
        this.lastLoginAt = null; // No login yet

        // providerUid is optional for EMAIL auth, but required for OAuth providers
        if (provider == Provider.EMAIL) {
            this.providerUid = providerUid;
        } else if (provider != null) {
            setProviderUid(providerUid);
        } else {
            this.providerUid = null;
        }
    }

    /**
     * Gets the unique authentication identifier.
     *
     * @return The auth ID (delegates to getId() from AbstractEntity)
     */
    public UUID getAuthId() {
        return getId();
    }

    /**
     * Sets the user ID with validation.
     *
     * @param userId The new user ID (cannot be null)
     * @throws IllegalArgumentException if userId is null
     */
    private void setUserId(UUID userId) {
        ValidationUtils.validateUUID(userId, "User ID");
        this.userId = userId;
    }

    /**
     * Gets the authentication provider.
     *
     * @return The provider
     */
    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        ValidationUtils.validateNotBlank(provider, "Authentication provider");
        this.provider = provider;
    }

    /**
     * Gets the provider-specific user ID.
     *
     * @return The provider UID (may be null for email auth)
     */
    public String getProviderUid() {
        return providerUid;
    }

    /**
     * Sets the provider UID with validation.
     *
     * @param providerUid The new provider UID
     * @throws IllegalArgumentException if validation fails
     */
    public void setProviderUid(String providerUid) {
        ValidationUtils.validateFormat(providerUid, RegexPatterns.PROVIDER_UID, "Provider UID");
        this.providerUid = providerUid;
    }

    /**
     * Gets the user's email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address with validation.
     *
     * @param email The new email address (must be valid format)
     * @throws IllegalArgumentException if email format is invalid
     */
    public void setEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        String trimmedEmail = email.trim();
        ValidationUtils.validateFormat(trimmedEmail, RegexPatterns.EMAIL, "Email");
        this.email = trimmedEmail;
    }

    /**
     * Gets the password hash.
     *
     * @return The password hash (may be null for OAuth providers)
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash with validation.
     *
     * @param passwordHash The new password hash (must meet requirements)
     * @throws IllegalArgumentException if validation fails
     */
    public void setPasswordHash(String passwordHash) {
        ValidationUtils.validateBcryptHash(passwordHash, "Password Hash");
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the last login timestamp.
     *
     * @return The last login timestamp (may be null)
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
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
     * Checks if this is OAuth-based authentication.
     *
     * @return true if provider is not EMAIL, false otherwise
     */
    public boolean isOAuthAuth() {
        return provider != Provider.EMAIL;
    }

    /**
     * Checks if this is email/password authentication.
     *
     * @return true if provider is EMAIL, false otherwise
     */
    public boolean isEmailAuth() {
        return provider == Provider.EMAIL;
    }

    /**
     * Checks if the user has ever logged in.
     *
     * @return true if lastLoginAt is not null, false otherwise
     */
    public boolean hasLoggedIn() {
        return lastLoginAt != null;
    }
}

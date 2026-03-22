package com.velora.app.core.domain.auth;

import java.util.UUID;
import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;
import com.velora.app.core.utils.RegexPatterns;

/**
 * Represents the main user profile in the Velora system.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public class User extends AbstractAuditableEntity {

    /**
     * Available user status values
     */
    public enum Status {
        ACTIVE,    // User can access the system
        SUSPENDED, // User temporarily blocked
        DELETED    // User marked for deletion
    }

    private String username;
    private String profileUrl;
    private String bio;
    private Status status;

    /**
     * Creates a new User with validation.
     *
     * @param id         The unique identifier for this user (cannot be null)
     * @param username   The username (3-30 chars, alphanumeric + underscore)
     * @param profileUrl Optional profile image URL (must be valid HTTP/HTTPS if provided)
     * @param bio        Optional user biography
     * @throws IllegalArgumentException if validation fails
     */
    public User(UUID id, String username, String profileUrl, String bio) {
        super(id);
        setUsername(username);
        setProfileUrl(profileUrl);
        setBio(bio);
        this.status = Status.ACTIVE; // Default to ACTIVE on creation
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username with validation.
     *
     * @param username The new username (must meet validation requirements)
     * @throws IllegalArgumentException if username is invalid
     */
    public void setUsername(String username) {
        ValidationUtils.validateFormat(username, RegexPatterns.USERNAME, "Username");
        this.username = username;
    }

    /**
     * Gets the profile URL.
     *
     * @return The profile URL (may be null)
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    /**
     * Sets the profile URL with validation.
     *
     * @param profileUrl The new profile URL (must be valid HTTP/HTTPS if provided)
     *                   or null/blank to clear the profile URL.
     * @throws IllegalArgumentException if URL format is invalid
     */
    public void setProfileUrl(String profileUrl) {
        if (profileUrl == null || profileUrl.isBlank()) {
            this.profileUrl = null;
            return;
        }
        ValidationUtils.validateUrl(profileUrl, "Profile URL");
        this.profileUrl = profileUrl;
    }

    /**
     * Gets the user biography.
     *
     * @return The user bio (may be null)
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the user biography.
     *
     * @param bio The new biography (can be null or empty)
     */
    public void setBio(String bio) {
        if (bio != null && !bio.isBlank()) {
            ValidationUtils.validateFormat(bio, RegexPatterns.BIO, "Bio");
        }
        this.bio = bio;
    }

    /**
     * Gets the current user status.
     *
     * @return The user status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Checks if the user is currently active.
     *
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    /**
     * Checks if the user is suspended.
     *
     * @return true if status is SUSPENDED, false otherwise
     */
    public boolean isSuspended() {
        return status == Status.SUSPENDED;
    }

    /**
     * Checks if the user is deleted.
     *
     * @return true if status is DELETED, false otherwise
     */
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    /**
     * Activates the user account.
     */
    public void activate() {
        this.status = Status.ACTIVE;
    }

    /**
     * Suspends the user account.
     */
    public void suspend() {
        this.status = Status.SUSPENDED;
    }

    /**
     * Marks the user account as deleted.
     */
    public void delete() {
        this.status = Status.DELETED;
    }
}

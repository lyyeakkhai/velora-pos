package com.velora.app.modules.authModule.service;

import com.velora.app.core.domain.auth.Membership;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.auth.User;
import com.velora.app.core.domain.auth.UserAuth;

import java.util.UUID;

/**
 * Application-layer contract for authentication and identity management.
 *
 * <p>Requirement: 16.1, 16.2
 */
public interface IAuthService {

    /**
     * Registers a new user with email/password credentials.
     */
    User registerUser(String username, String email, String rawPassword);

    /**
     * Registers a new user via OAuth provider.
     */
    User registerOAuth(String username, String email, String provider, String providerUid);

    /**
     * Authenticates a user by email and raw password.
     */
    UserAuth login(String email, String rawPassword);

    /**
     * Assigns a shop membership role to a user. Requires OWNER or SUPER_ADMIN.
     */
    Membership assignMembership(UUID actorId, UUID userId, UUID shopId, Role.RoleName role);

    /**
     * Revokes a membership. Requires OWNER or SUPER_ADMIN.
     */
    void revokeMembership(UUID actorId, UUID membershipId);

    /**
     * Updates a user's status. Requires SUPER_ADMIN.
     */
    User updateUserStatus(UUID actorId, UUID userId, User.Status newStatus);
}

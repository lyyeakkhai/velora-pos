package com.velora.app.common;

import com.velora.app.modules.authModule.domain.Role;

/**
 * Polymorphic interface for role-based access control.
 *
 * All access policy classes implement this interface, enabling the application
 * layer to depend on a single abstraction rather than concrete policy types.
 *
 * Implementations: NotificationAccessPolicy, FeedbackAccessPolicy,
 * RolePolicy (inventory), AnalyticsAccessPolicy
 *
 * Requirements: 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7
 */
public interface AccessPolicy {

    /**
     * Checks whether the given actor role is permitted to perform the specified
     * operation.
     *
     * @param actorRole the role of the acting user
     * @param operation the operation being attempted
     * @throws DomainException if the actor role is not permitted
     */
    void check(Role.RoleName actorRole, String operation);
}

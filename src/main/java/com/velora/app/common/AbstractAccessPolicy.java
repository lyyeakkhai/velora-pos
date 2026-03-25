package com.velora.app.common;

import com.velora.app.modules.authModule.domain.Role;

/**
 * Abstract base class for all role-based access policy classes.
 *
 * Declares the abstract {@code check()} method that each subclass must
 * implement
 * with domain-specific role rules. Provides a concrete {@code requireAdmin()}
 * method
 * that delegates to {@code check()} using the "ADMIN_ONLY" operation sentinel.
 *
 * Subclasses: NotificationAccessPolicy, FeedbackAccessPolicy, RolePolicy,
 * AnalyticsAccessPolicy
 *
 * Requirements: 9.1, 9.2
 */
public abstract class AbstractAccessPolicy {

    /**
     * Checks whether the given actor role is permitted to perform the specified
     * operation.
     *
     * @param actorRole the role of the acting user
     * @param operation the operation being attempted
     * @throws DomainException if the actor role is not permitted
     */
    public abstract void check(Role.RoleName actorRole, String operation);

    /**
     * Asserts that the actor holds an admin-level role.
     * Delegates to {@code check(actorRole, "ADMIN_ONLY")}.
     *
     * @param actorRole the role of the acting user
     * @throws DomainException if the actor is not an admin
     */
    public void requireAdmin(Role.RoleName actorRole) {
        check(actorRole, "ADMIN_ONLY");
    }
}

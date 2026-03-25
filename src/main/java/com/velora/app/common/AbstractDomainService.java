package com.velora.app.common;

import com.velora.app.modules.authModule.domain.Role;

/**
 * Abstract base class for domain services.
 *
 * Provides shared guard methods for role authorization and null validation,
 * eliminating inline checks duplicated across domain services.
 *
 * Subclasses: NotificationService, FeedbackService, ReportingService,
 * AnalyticsAggregationService
 *
 * Requirements: 8.1, 8.2, 8.7, 8.8
 */
public abstract class AbstractDomainService {

    /**
     * Asserts that the actual role is one of the allowed roles.
     *
     * @param actual  the role of the acting user
     * @param allowed one or more permitted roles
     * @throws DomainException if actual is not in the allowed list
     */
    protected void requireRole(Role.RoleName actual, Role.RoleName... allowed) {
        for (Role.RoleName permitted : allowed) {
            if (permitted == actual)
                return;
        }
        throw new DomainException("Role " + actual + " is not authorized for this operation");
    }

    /**
     * Asserts that the given value is not null.
     *
     * @param value     the value to check
     * @param fieldName the name of the field, included in the exception message
     * @throws DomainException if value is null
     */
    protected void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new DomainException(fieldName + " must not be null");
        }
    }
}

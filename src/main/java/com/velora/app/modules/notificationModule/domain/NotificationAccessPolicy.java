package com.velora.app.modules.notificationModule.domain;

import com.velora.app.common.AbstractAccessPolicy;
import com.velora.app.common.AccessPolicy;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Access-control rules for notifications.
 * Extends AbstractAccessPolicy; implements check() with notification-specific role rules.
 * Requirements: 9.3
 */
public class NotificationAccessPolicy extends AbstractAccessPolicy implements AccessPolicy {

    @Override
    public void check(Role.RoleName actorRole, String operation) {
        if (actorRole == Role.RoleName.SUPER_ADMIN) return;
        switch (operation) {
            case "ADMIN_ONLY":
                throw new DomainException("SUPER_ADMIN role required for: " + operation);
            case "CREATE_NOTIFICATION":
                throw new DomainException("Only SYSTEM or SUPER_ADMIN can create notifications");
            case "READ_INBOX":
                if (actorRole == null) throw new DomainException("actorRole must not be null");
                break;
            default:
                throw new DomainException("Unknown operation: " + operation);
        }
    }

    public void requireReadableInbox(Role.RoleName actorRole, boolean systemActor) {
        if (systemActor) throw new DomainException("SYSTEM cannot read user inbox");
        ValidationUtils.validateNotBlank(actorRole, "actorRole");
        if (actorRole == Role.RoleName.SUPER_ADMIN) throw new DomainException("ADMIN has no access to private alerts");
    }

    public void requireUserScope(UUID actorUserId, UUID userId) {
        ValidationUtils.validateUUID(actorUserId, "actorUserId");
        ValidationUtils.validateUUID(userId, "userId");
        if (!actorUserId.equals(userId)) throw new DomainException("No cross-user access");
    }

    public void requireCanCreate(Role.RoleName actorRole, boolean systemActor, NotificationType type) {
        NotificationValidation.validateType(type);
        if (systemActor) return;
        ValidationUtils.validateNotBlank(actorRole, "actorRole");
        if (actorRole == Role.RoleName.SUPER_ADMIN) {
            if (type != NotificationType.SYSTEM) throw new DomainException("ADMIN can create SYSTEM notifications only");
            return;
        }
        throw new DomainException("Only SYSTEM or ADMIN can create notifications");
    }
}

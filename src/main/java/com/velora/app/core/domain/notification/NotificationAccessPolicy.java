package com.velora.app.core.domain.notification;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Central access-control rules for notifications.
 */
public final class NotificationAccessPolicy {

    private NotificationAccessPolicy() {
    }

    public static void requireReadableInbox(Role.RoleName actorRole, boolean systemActor) {
        if (systemActor) {
            throw new IllegalStateException("SYSTEM cannot read user inbox");
        }
        ValidationUtils.validateNotBlank(actorRole, "actorRole");
        if (actorRole == Role.RoleName.SUPER_ADMIN) {
            throw new IllegalStateException("ADMIN has no access to private alerts");
        }
    }

    public static void requireUserScope(UUID actorUserId, UUID userId) {
        ValidationUtils.validateUUID(actorUserId, "actorUserId");
        ValidationUtils.validateUUID(userId, "userId");
        if (!actorUserId.equals(userId)) {
            throw new IllegalStateException("No cross-user access");
        }
    }

    public static void requireCanCreate(Role.RoleName actorRole, boolean systemActor, NotificationType type) {
        NotificationValidation.validateType(type);
        if (systemActor) {
            return;
        }
        ValidationUtils.validateNotBlank(actorRole, "actorRole");
        if (actorRole == Role.RoleName.SUPER_ADMIN) {
            if (type != NotificationType.SYSTEM) {
                throw new IllegalStateException("ADMIN can create SYSTEM notifications only");
            }
            return;
        }
        throw new IllegalStateException("Only SYSTEM or ADMIN can create notifications");
    }
}

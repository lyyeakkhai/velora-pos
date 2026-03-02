package com.velora.app.core.domain.feedback;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

public final class FeedbackAccessPolicy {

    private FeedbackAccessPolicy() {
    }

    public static void requireAdmin(Role.RoleName roleName) {
        ValidationUtils.validateNotBlank(roleName, "roleName");
        if (roleName != Role.RoleName.SUPER_ADMIN) {
            throw new IllegalStateException("SUPER_ADMIN role required");
        }
    }

    public static void requireOwner(UUID actorUserId, UUID suggestionOwnerUserId) {
        ValidationUtils.validateUUID(actorUserId, "actorUserId");
        ValidationUtils.validateUUID(suggestionOwnerUserId, "suggestionOwnerUserId");
        if (!actorUserId.equals(suggestionOwnerUserId)) {
            throw new IllegalStateException("Only the owner can modify this suggestion");
        }
    }
}

package com.velora.app.modules.feedback.domain;

import com.velora.app.common.AbstractAccessPolicy;
import com.velora.app.common.AccessPolicy;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Access-control rules for feedback/suggestions.
 * Extends AbstractAccessPolicy; implements check() with feedback-specific role rules.
 * Requirements: 9.4
 */
public class FeedbackAccessPolicy extends AbstractAccessPolicy implements AccessPolicy {

    @Override
    public void check(Role.RoleName actorRole, String operation) {
        if (actorRole == Role.RoleName.SUPER_ADMIN) return;
        switch (operation) {
            case "ADMIN_ONLY":
                throw new DomainException("SUPER_ADMIN role required for: " + operation);
            case "EDIT_SUGGESTION":
                if (actorRole == null) throw new DomainException("actorRole must not be null");
                break;
            default:
                throw new DomainException("Unknown operation: " + operation);
        }
    }

    public void requireOwner(UUID actorUserId, UUID suggestionOwnerUserId) {
        ValidationUtils.validateUUID(actorUserId, "actorUserId");
        ValidationUtils.validateUUID(suggestionOwnerUserId, "suggestionOwnerUserId");
        if (!actorUserId.equals(suggestionOwnerUserId)) throw new DomainException("Only the owner can modify this suggestion");
    }
}

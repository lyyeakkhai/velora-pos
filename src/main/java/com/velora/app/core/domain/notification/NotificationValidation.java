package com.velora.app.core.domain.notification;

import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Centralized validation rules for notification models.
 */
public final class NotificationValidation {

    private NotificationValidation() {
    }

    public static void validateUserId(UUID userId) {
        ValidationUtils.validateUUID(userId, "userId");
    }

    public static void validateTitle(String title) {
        ValidationUtils.validateNotBlank(title, "title");
        String trimmed = title.trim();
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("title max length is 255");
        }
    }

    public static void validateContent(String content) {
        ValidationUtils.validateNotBlank(content, "content");
    }

    public static void validateLinkUrlNullable(String linkUrl) {
        if (linkUrl == null) {
            return;
        }
        if (linkUrl.isBlank()) {
            throw new IllegalArgumentException("linkUrl cannot be blank");
        }
        ValidationUtils.validateUrl(linkUrl, "linkUrl");
    }

    public static void validateType(NotificationType type) {
        ValidationUtils.validateNotBlank(type, "type");
    }

    public static void validatePriority(NotificationPriority priority) {
        ValidationUtils.validateNotBlank(priority, "priority");
    }
}

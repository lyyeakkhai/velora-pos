package com.velora.app.core.domain.notification;

import com.velora.app.common.AbstractAuditableEntity;
import java.time.Clock;
import java.util.UUID;

/**
 * Append-only core notification record.
 */
public class Notification extends AbstractAuditableEntity {

    private final UUID userId;

    private final NotificationType type;
    private final NotificationPriority priority;
    private final String title;
    private final String content;
    private final String linkUrl;

    private boolean isRead;
    private boolean isArchived;

    public Notification(UUID notificationId, UUID userId, NotificationType type, NotificationPriority priority,
            String title, String content, String linkUrl, Clock clock) {
        super(notificationId);
        NotificationValidation.validateUserId(userId);
        NotificationValidation.validateType(type);
        NotificationValidation.validatePriority(priority);
        NotificationValidation.validateTitle(title);
        NotificationValidation.validateContent(content);
        NotificationValidation.validateLinkUrlNullable(linkUrl);

        this.userId = userId;
        this.type = type;
        this.priority = priority;
        this.title = title.trim();
        this.content = content.trim();
        this.linkUrl = linkUrl == null ? null : linkUrl.trim();
        this.isRead = false;
        this.isArchived = false;
    }

    public UUID getNotificationId() {
        return getId();
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markRead() {
        this.isRead = true;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void archive() {
        this.isArchived = true;
    }
}

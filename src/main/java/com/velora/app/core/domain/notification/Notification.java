package com.velora.app.core.domain.notification;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Append-only core notification record.
 */
public class Notification {

    private final UUID notificationId;
    private final UUID userId;
    private final LocalDateTime createdAt;

    private final NotificationType type;
    private final NotificationPriority priority;
    private final String title;
    private final String content;
    private final String linkUrl;

    private boolean isRead;
    private boolean isArchived;

    public Notification(UUID notificationId, UUID userId, NotificationType type, NotificationPriority priority,
            String title, String content, String linkUrl, Clock clock) {
        NotificationValidation.validateUserId(userId);
        NotificationValidation.validateType(type);
        NotificationValidation.validatePriority(priority);
        NotificationValidation.validateTitle(title);
        NotificationValidation.validateContent(content);
        NotificationValidation.validateLinkUrlNullable(linkUrl);

        if (notificationId == null) {
            throw new IllegalArgumentException("notificationId cannot be null");
        }

        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.priority = priority;
        this.title = title.trim();
        this.content = content.trim();
        this.linkUrl = linkUrl == null ? null : linkUrl.trim();
        this.isRead = false;
        this.isArchived = false;
        this.createdAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
    }

    public UUID getNotificationId() {
        return notificationId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notification that = (Notification) o;
        return notificationId.equals(that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }
}

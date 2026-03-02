package com.velora.app.core.domain.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    void append(Notification notification);

    Optional<Notification> findById(UUID notificationId);

    /**
     * Returns newest-first notifications for a user.
     */
    List<Notification> findUserNotifications(UUID userId, int limit, LocalDateTime beforeCreatedAtExclusive);

    long countUnread(UUID userId);

    void markRead(UUID userId, UUID notificationId);

    int markAllRead(UUID userId);
}

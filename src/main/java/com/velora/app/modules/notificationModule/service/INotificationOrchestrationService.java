package com.velora.app.modules.notificationModule.service;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.modules.notificationModule.domain.Notification;
import com.velora.app.modules.notificationModule.domain.NotificationPriority;
import com.velora.app.modules.notificationModule.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer contract for notification inbox management and dispatch.
 *
 * <p>Requirement: 16.1, 16.8
 */
public interface INotificationOrchestrationService {

    /**
     * Creates and dispatches a notification to the target user.
     */
    Notification sendNotification(Role.RoleName actorRole, boolean systemActor, UUID userId,
            NotificationType type, NotificationPriority priority, String title, String content, String linkUrl);

    /**
     * Marks a single notification as read.
     */
    void markRead(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId, UUID notificationId);

    /**
     * Marks all notifications for a user as read.
     *
     * @return number of notifications marked
     */
    int markAllRead(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId);

    /**
     * Returns the count of unread notifications for a user.
     */
    long getUnreadCount(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId);

    /**
     * Returns paginated notifications for a user, newest-first.
     */
    List<Notification> getNotifications(Role.RoleName actorRole, boolean systemActor, UUID actorUserId,
            UUID userId, int limit, LocalDateTime beforeCreatedAtExclusive);

    /**
     * Updates notification preferences for a user.
     */
    void updatePreferences(UUID userId, boolean emailEnabled, boolean billingAlerts);

    /**
     * Retries all pending failed dispatch records.
     */
    void retryFailedDispatches();
}

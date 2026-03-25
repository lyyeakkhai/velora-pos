package com.velora.app.modules.notificationModule.service;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.common.DomainException;
import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.notificationModule.domain.DispatchService;
import com.velora.app.modules.notificationModule.domain.Notification;
import com.velora.app.modules.notificationModule.domain.NotificationPreferences;
import com.velora.app.modules.notificationModule.domain.NotificationPreferencesRepository;
import com.velora.app.modules.notificationModule.domain.NotificationPriority;
import com.velora.app.modules.notificationModule.domain.NotificationService;
import com.velora.app.modules.notificationModule.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer service for notification inbox management and dispatch.
 *
 * <p>Extends {@link AbstractDomainService} to reuse {@code requireNotNull} guard methods.
 * Delegates domain logic to {@link NotificationService} and {@link DispatchService}.
 *
 * <p>Requirements: 16.8
 */
public class NotificationOrchestrationService extends AbstractDomainService
        implements INotificationOrchestrationService {

    private final NotificationService notificationService;
    private final DispatchService dispatchService;
    private final NotificationPreferencesRepository preferencesRepository;

    public NotificationOrchestrationService(
            NotificationService notificationService,
            DispatchService dispatchService,
            NotificationPreferencesRepository preferencesRepository) {
        requireNotNull(notificationService, "notificationService");
        requireNotNull(dispatchService, "dispatchService");
        requireNotNull(preferencesRepository, "preferencesRepository");
        this.notificationService = notificationService;
        this.dispatchService = dispatchService;
        this.preferencesRepository = preferencesRepository;
    }

    /**
     * Creates and dispatches a notification to the target user.
     */
    @Override
    public Notification sendNotification(Role.RoleName actorRole, boolean systemActor, UUID userId,
            NotificationType type, NotificationPriority priority, String title, String content, String linkUrl) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(userId, "userId");
        requireNotNull(type, "type");
        requireNotNull(priority, "priority");
        requireNotNull(title, "title");
        requireNotNull(content, "content");

        Notification notification = notificationService.createNotification(
                actorRole, systemActor, userId, type, priority, title, content, linkUrl);

        dispatchService.scheduleDelivery(100);

        return notification;
    }

    /**
     * Marks a single notification as read.
     */
    @Override
    public void markRead(Role.RoleName actorRole, boolean systemActor, UUID actorUserId,
            UUID userId, UUID notificationId) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(userId, "userId");
        requireNotNull(notificationId, "notificationId");

        notificationService.markAsRead(actorRole, systemActor, actorUserId, userId, notificationId);
    }

    /**
     * Marks all notifications for a user as read.
     *
     * @return number of notifications marked
     */
    @Override
    public int markAllRead(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(userId, "userId");

        return notificationService.markAllAsRead(actorRole, systemActor, actorUserId, userId);
    }

    /**
     * Returns the count of unread notifications for a user.
     */
    @Override
    public long getUnreadCount(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(userId, "userId");

        return notificationService.getUnreadCount(actorRole, systemActor, actorUserId, userId);
    }

    /**
     * Returns paginated notifications for a user, newest-first.
     */
    @Override
    public List<Notification> getNotifications(Role.RoleName actorRole, boolean systemActor, UUID actorUserId,
            UUID userId, int limit, LocalDateTime beforeCreatedAtExclusive) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(userId, "userId");

        return notificationService.getUserNotifications(
                actorRole, systemActor, actorUserId, userId, limit, beforeCreatedAtExclusive);
    }

    /**
     * Updates notification preferences for a user.
     * Creates default preferences if none exist yet.
     */
    @Override
    public void updatePreferences(UUID userId, boolean emailEnabled, boolean billingAlerts) {
        requireNotNull(userId, "userId");

        NotificationPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreferences.defaults(userId));

        prefs.setEmailEnabled(emailEnabled);
        // billingAlerts is always true by domain invariant; ignore the parameter
        preferencesRepository.save(prefs);
    }

    /**
     * Retries all pending failed dispatch records.
     */
    @Override
    public void retryFailedDispatches() {
        dispatchService.retryFailed(500);
    }
}

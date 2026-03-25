package com.velora.app.modules.notificationModule.domain;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Business service for in-app notification inbox.
 */
public class NotificationService extends AbstractDomainService {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatchRepository dispatchRepository;
    private final Clock clock;
    private final NotificationAccessPolicy policy;

    public NotificationService(NotificationRepository notificationRepository,
            NotificationDispatchRepository dispatchRepository,
            Clock clock) {
        requireNotNull(notificationRepository, "notificationRepository");
        requireNotNull(dispatchRepository, "dispatchRepository");
        this.notificationRepository = notificationRepository;
        this.dispatchRepository = dispatchRepository;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.policy = new NotificationAccessPolicy();
    }

    /**
     * Creates an append-only notification record. Persisted before any dispatch.
     */
    public Notification createNotification(Role.RoleName actorRole, boolean systemActor, UUID userId,
            NotificationType type, NotificationPriority priority, String title, String content, String linkUrl) {
        policy.requireCanCreate(actorRole, systemActor, type);
        NotificationValidation.validateUserId(userId);
        NotificationValidation.validateType(type);
        NotificationValidation.validatePriority(priority);
        NotificationValidation.validateTitle(title);
        NotificationValidation.validateContent(content);
        NotificationValidation.validateLinkUrlNullable(linkUrl);

        Notification notification = new Notification(UUID.randomUUID(), userId, type, priority, title, content, linkUrl,
                clock);
        notificationRepository.append(notification);

        // In-app is always available. Record dispatch idempotently for audit.
        dispatchRepository.createIfAbsent(NotificationDispatchRecord.createNow(notification.getNotificationId(),
                NotificationChannel.IN_APP, clock));

        // Email dispatch pipeline is only considered for HIGH priority.
        if (priority == NotificationPriority.HIGH) {
            dispatchRepository.createIfAbsent(NotificationDispatchRecord.createNow(notification.getNotificationId(),
                    NotificationChannel.EMAIL, clock));
        }
        return notification;
    }

    public void markAsRead(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId,
            UUID notificationId) {
        policy.requireReadableInbox(actorRole, systemActor);
        policy.requireUserScope(actorUserId, userId);
        ValidationUtils.validateUUID(notificationId, "notificationId");
        notificationRepository.markRead(userId, notificationId);
    }

    public int markAllAsRead(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId) {
        policy.requireReadableInbox(actorRole, systemActor);
        policy.requireUserScope(actorUserId, userId);
        return notificationRepository.markAllRead(userId);
    }

    public long getUnreadCount(Role.RoleName actorRole, boolean systemActor, UUID actorUserId, UUID userId) {
        policy.requireReadableInbox(actorRole, systemActor);
        policy.requireUserScope(actorUserId, userId);
        return notificationRepository.countUnread(userId);
    }

    /**
     * Cursor pagination: newest-first; optionally provide an exclusive upper bound for createdAt.
     */
    public List<Notification> getUserNotifications(Role.RoleName actorRole, boolean systemActor, UUID actorUserId,
            UUID userId, int limit, LocalDateTime beforeCreatedAtExclusive) {
        policy.requireReadableInbox(actorRole, systemActor);
        policy.requireUserScope(actorUserId, userId);

        if (limit <= 0 || limit > 200) {
            throw new IllegalArgumentException("limit must be in 1..200");
        }
        return notificationRepository.findUserNotifications(userId, limit, beforeCreatedAtExclusive);
    }
}

package com.velora.app.infrastructure.db;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.velora.app.modules.notificationModule.domain.Notification;
import com.velora.app.modules.notificationModule.domain.NotificationRepository;

/**
 * PostgreSQL implementation of NotificationRepository.
 * Requirements: 14.7
 */
public class PostgresNotificationRepository implements NotificationRepository {

    @Override
    public void append(Notification notification) {
        // TODO: implement JDBC insert
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Notification> findById(UUID notificationId) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Notification> findUserNotifications(UUID userId, int limit, LocalDateTime beforeCreatedAtExclusive) {
        // TODO: implement JDBC select with pagination
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long countUnread(UUID userId) {
        // TODO: implement JDBC count query
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void markRead(UUID userId, UUID notificationId) {
        // TODO: implement JDBC update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int markAllRead(UUID userId) {
        // TODO: implement JDBC bulk update
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

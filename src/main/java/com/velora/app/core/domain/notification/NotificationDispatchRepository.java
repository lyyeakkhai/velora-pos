package com.velora.app.core.domain.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDispatchRepository {

    Optional<NotificationDispatchRecord> findRecord(UUID notificationId, NotificationChannel channel);

    /**
     * Creates a record if absent; returns the existing record otherwise.
     */
    NotificationDispatchRecord createIfAbsent(NotificationDispatchRecord record);

    void saveRecord(NotificationDispatchRecord record);

    void appendLog(NotificationDispatchLog log);

    List<NotificationDispatchRecord> findDue(LocalDateTime nowInclusive, int limit);

    List<NotificationDispatchRecord> findFailed(int limit);
}

package com.velora.app.modules.notificationModule.domain;

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

    /** Persists an updated dispatch record. */
    void save(NotificationDispatchRecord record);

    /**
     * @deprecated Use {@link #save(NotificationDispatchRecord)} instead.
     */
    @Deprecated
    default void saveRecord(NotificationDispatchRecord record) {
        save(record);
    }

    void appendLog(NotificationDispatchLog log);

    /**
     * Returns records that are pending dispatch (due by {@code nowInclusive}).
     */
    List<NotificationDispatchRecord> findPending(LocalDateTime nowInclusive, int limit);

    /**
     * @deprecated Use {@link #findPending(LocalDateTime, int)} instead.
     */
    @Deprecated
    default List<NotificationDispatchRecord> findDue(LocalDateTime nowInclusive, int limit) {
        return findPending(nowInclusive, limit);
    }

    List<NotificationDispatchRecord> findFailed(int limit);
}

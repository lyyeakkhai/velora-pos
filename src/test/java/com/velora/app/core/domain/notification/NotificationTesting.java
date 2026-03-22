package com.velora.app.core.domain.notification;

import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

import static org.junit.Assert.*;

public class NotificationTesting {

    private static class InMemoryNotificationRepository implements NotificationRepository {
        private final Map<UUID, Notification> byId = new HashMap<>();

        @Override
        public void append(Notification notification) {
            byId.put(notification.getNotificationId(), notification);
        }

        @Override
        public Optional<Notification> findById(UUID notificationId) {
            return Optional.ofNullable(byId.get(notificationId));
        }

        @Override
        public List<Notification> findUserNotifications(UUID userId, int limit, LocalDateTime beforeCreatedAtExclusive) {
            List<Notification> out = new ArrayList<>();
            for (Notification notification : byId.values()) {
                if (!notification.getUserId().equals(userId)) {
                    continue;
                }
                if (beforeCreatedAtExclusive != null && !notification.getCreatedAt().isBefore(beforeCreatedAtExclusive)) {
                    continue;
                }
                out.add(notification);
            }
            out.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
            if (out.size() > limit) {
                return out.subList(0, limit);
            }
            return out;
        }

        @Override
        public long countUnread(UUID userId) {
            long count = 0;
            for (Notification notification : byId.values()) {
                if (notification.getUserId().equals(userId) && !notification.isRead()) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public void markRead(UUID userId, UUID notificationId) {
            Notification notification = byId.get(notificationId);
            if (notification == null || !notification.getUserId().equals(userId)) {
                throw new IllegalStateException("notification not found");
            }
            notification.markRead();
        }

        @Override
        public int markAllRead(UUID userId) {
            int changed = 0;
            for (Notification notification : byId.values()) {
                if (notification.getUserId().equals(userId) && !notification.isRead()) {
                    notification.markRead();
                    changed++;
                }
            }
            return changed;
        }
    }

    private static class InMemoryPreferencesRepository implements NotificationPreferencesRepository {
        private final Map<UUID, NotificationPreferences> byUser = new HashMap<>();

        @Override
        public Optional<NotificationPreferences> findByUserId(UUID userId) {
            return Optional.ofNullable(byUser.get(userId));
        }

        @Override
        public void save(NotificationPreferences preferences) {
            byUser.put(preferences.getUserId(), preferences);
        }
    }

    private static class InMemoryDispatchRepository implements NotificationDispatchRepository {
        private final Map<String, NotificationDispatchRecord> records = new HashMap<>();
        private final List<NotificationDispatchLog> logs = new ArrayList<>();

        private static String key(UUID id, NotificationChannel channel) {
            return id + ":" + channel.name();
        }

        @Override
        public Optional<NotificationDispatchRecord> findRecord(UUID notificationId, NotificationChannel channel) {
            return Optional.ofNullable(records.get(key(notificationId, channel)));
        }

        @Override
        public NotificationDispatchRecord createIfAbsent(NotificationDispatchRecord record) {
            String k = key(record.getNotificationId(), record.getChannel());
            NotificationDispatchRecord existing = records.get(k);
            if (existing != null) {
                return existing;
            }
            records.put(k, record);
            return record;
        }

        @Override
        public void saveRecord(NotificationDispatchRecord record) {
            records.put(key(record.getNotificationId(), record.getChannel()), record);
        }

        @Override
        public void appendLog(NotificationDispatchLog log) {
            logs.add(log);
        }

        @Override
        public List<NotificationDispatchRecord> findDue(LocalDateTime nowInclusive, int limit) {
            List<NotificationDispatchRecord> out = new ArrayList<>();
            for (NotificationDispatchRecord record : records.values()) {
                if (record.isTerminal()) {
                    continue;
                }
                LocalDateTime due = record.getNextAttemptAt();
                if (due != null && (due.isBefore(nowInclusive) || due.isEqual(nowInclusive))) {
                    out.add(record);
                }
            }
            if (out.size() > limit) {
                return out.subList(0, limit);
            }
            return out;
        }

        @Override
        public List<NotificationDispatchRecord> findFailed(int limit) {
            List<NotificationDispatchRecord> out = new ArrayList<>();
            for (NotificationDispatchRecord record : records.values()) {
                if (record.getStatus() == DispatchStatus.FAILED) {
                    out.add(record);
                }
            }
            if (out.size() > limit) {
                return out.subList(0, limit);
            }
            return out;
        }

        int logCount() {
            return logs.size();
        }
    }

    private static class RecordingEmailGateway implements EmailGateway {
        private final List<String> sent = new ArrayList<>();
        private boolean fail;

        @Override
        public void sendHighPriorityEmail(UUID userId, String title, String content, String linkUrl) {
            if (fail) {
                throw new RuntimeException("SMTP failure");
            }
            sent.add(userId + ":" + title);
        }

        int sentCount() {
            return sent.size();
        }

        void setFail(boolean fail) {
            this.fail = fail;
        }
    }

    @Test
    public void createNotification_persistsAndCreatesDispatchRecords() {
        InMemoryNotificationRepository notificationRepo = new InMemoryNotificationRepository();
        InMemoryDispatchRepository dispatchRepo = new InMemoryDispatchRepository();
        Clock fixed = Clock.fixed(Instant.parse("2026-03-02T00:00:00Z"), ZoneOffset.UTC);
        NotificationService service = new NotificationService(notificationRepo, dispatchRepo, fixed);
        UUID userId = UUID.randomUUID();

        Notification normal = service.createNotification(null, true, userId, NotificationType.TRANSACTIONAL,
                NotificationPriority.NORMAL, "Title", "Content", null);

        assertNotNull(normal.getNotificationId());
        assertEquals(1, notificationRepo.findUserNotifications(userId, 20, null).size());
        assertTrue(dispatchRepo.findRecord(normal.getNotificationId(), NotificationChannel.IN_APP).isPresent());
        assertFalse(dispatchRepo.findRecord(normal.getNotificationId(), NotificationChannel.EMAIL).isPresent());

        Notification high = service.createNotification(null, true, userId, NotificationType.SYSTEM,
                NotificationPriority.HIGH, "High", "Content", "https://example.com");
        assertTrue(dispatchRepo.findRecord(high.getNotificationId(), NotificationChannel.EMAIL).isPresent());
    }

    @Test
    public void inboxAccess_isUserScoped_andAdminCannotRead() {
        InMemoryNotificationRepository notificationRepo = new InMemoryNotificationRepository();
        InMemoryDispatchRepository dispatchRepo = new InMemoryDispatchRepository();
        NotificationService service = new NotificationService(notificationRepo, dispatchRepo, Clock.systemUTC());
        UUID userId = UUID.randomUUID();
        Notification notification = service.createNotification(null, true, userId, NotificationType.SYSTEM,
                NotificationPriority.NORMAL, "T", "C", null);

        // Cross-user read protection
        try {
            service.getUnreadCount(Role.RoleName.OWNER, false, UUID.randomUUID(), userId);
            fail("Expected exception");
        } catch (DomainException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("cross"));
        }

        // Admin cannot read
        try {
            service.getUnreadCount(Role.RoleName.SUPER_ADMIN, false, userId, userId);
            fail("Expected exception");
        } catch (DomainException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("admin"));
        }

        assertEquals(1, service.getUnreadCount(Role.RoleName.OWNER, false, userId, userId));
        service.markAsRead(Role.RoleName.OWNER, false, userId, userId, notification.getNotificationId());
        assertEquals(0, service.getUnreadCount(Role.RoleName.OWNER, false, userId, userId));
    }

    @Test
    public void dispatch_emailOnlyForHigh_andRespectsEmailEnabled_andIsIdempotent() {
        InMemoryNotificationRepository notificationRepo = new InMemoryNotificationRepository();
        InMemoryPreferencesRepository prefsRepo = new InMemoryPreferencesRepository();
        InMemoryDispatchRepository dispatchRepo = new InMemoryDispatchRepository();
        RecordingEmailGateway gateway = new RecordingEmailGateway();
        Clock fixed = Clock.fixed(Instant.parse("2026-03-02T00:00:00Z"), ZoneOffset.UTC);

        NotificationService notificationService = new NotificationService(notificationRepo, dispatchRepo, fixed);
        PreferenceService preferenceService = new PreferenceService(prefsRepo, fixed);
        DispatchService dispatchService = new DispatchService(notificationRepo, prefsRepo, dispatchRepo, gateway, fixed);

        UUID userId = UUID.randomUUID();
        // disabled email -> email dispatch should be skipped (not retried later)
        preferenceService.updatePreferences(Role.RoleName.OWNER, false, userId, userId, false, false);

        Notification highSkipped = notificationService.createNotification(null, true, userId, NotificationType.SYSTEM,
            NotificationPriority.HIGH, "High", "C", null);

        dispatchService.dispatchNotification(highSkipped.getNotificationId(), NotificationChannel.EMAIL);
        assertEquals(0, gateway.sentCount());

        // enabling later should not back-send skipped notifications
        preferenceService.updatePreferences(Role.RoleName.OWNER, false, userId, userId, true, false);
        dispatchService.dispatchNotification(highSkipped.getNotificationId(), NotificationChannel.EMAIL);
        assertEquals(0, gateway.sentCount());

        // email enabled at dispatch time -> send exactly once (idempotent)
        Notification highSent = notificationService.createNotification(null, true, userId, NotificationType.SYSTEM,
            NotificationPriority.HIGH, "High2", "C", null);
        dispatchService.dispatchNotification(highSent.getNotificationId(), NotificationChannel.EMAIL);
        assertEquals(1, gateway.sentCount());
        dispatchService.dispatchNotification(highSent.getNotificationId(), NotificationChannel.EMAIL);
        assertEquals(1, gateway.sentCount());
    }

    @Test
    public void dispatch_failureIsLoggedAndRetryable() {
        InMemoryNotificationRepository notificationRepo = new InMemoryNotificationRepository();
        InMemoryPreferencesRepository prefsRepo = new InMemoryPreferencesRepository();
        InMemoryDispatchRepository dispatchRepo = new InMemoryDispatchRepository();
        RecordingEmailGateway gateway = new RecordingEmailGateway();
        Clock fixed = Clock.fixed(Instant.parse("2026-03-02T00:00:00Z"), ZoneOffset.UTC);

        NotificationService notificationService = new NotificationService(notificationRepo, dispatchRepo, fixed);
        PreferenceService preferenceService = new PreferenceService(prefsRepo, fixed);
        DispatchService dispatchService = new DispatchService(notificationRepo, prefsRepo, dispatchRepo, gateway, fixed);

        UUID userId = UUID.randomUUID();
        preferenceService.updatePreferences(Role.RoleName.OWNER, false, userId, userId, true, false);
        Notification high = notificationService.createNotification(null, true, userId, NotificationType.SYSTEM,
                NotificationPriority.HIGH, "High", "C", null);

        gateway.setFail(true);
        dispatchService.dispatchNotification(high.getNotificationId(), NotificationChannel.EMAIL);
        assertEquals(0, gateway.sentCount());
        assertTrue(dispatchRepo.logCount() >= 1);

        gateway.setFail(false);
        dispatchService.retryFailed(10);
        assertEquals(1, gateway.sentCount());
    }
}

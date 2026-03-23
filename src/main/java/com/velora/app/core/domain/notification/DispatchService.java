package com.velora.app.core.domain.notification;

import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Dispatch orchestration service.
 * Requirements: 17.4, 17.5
 */
public class DispatchService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final NotificationDispatchRepository dispatchRepository;
    private final EmailGateway emailGateway;
    private final List<NotificationSender> senders;
    private final Clock clock;

    public DispatchService(NotificationRepository notificationRepository,
            NotificationPreferencesRepository preferencesRepository,
            NotificationDispatchRepository dispatchRepository,
            EmailGateway emailGateway,
            Clock clock) {
        this(notificationRepository, preferencesRepository, dispatchRepository, emailGateway,
                List.of(), clock);
    }

    public DispatchService(NotificationRepository notificationRepository,
            NotificationPreferencesRepository preferencesRepository,
            NotificationDispatchRepository dispatchRepository,
            EmailGateway emailGateway,
            List<NotificationSender> senders,
            Clock clock) {
        ValidationUtils.validateNotBlank(notificationRepository, "notificationRepository");
        ValidationUtils.validateNotBlank(preferencesRepository, "preferencesRepository");
        ValidationUtils.validateNotBlank(dispatchRepository, "dispatchRepository");
        ValidationUtils.validateNotBlank(emailGateway, "emailGateway");
        this.notificationRepository = notificationRepository;
        this.preferencesRepository = preferencesRepository;
        this.dispatchRepository = dispatchRepository;
        this.emailGateway = emailGateway;
        this.senders = senders == null ? List.of() : List.copyOf(senders);
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    /**
     * Dispatches a notification to all eligible senders polymorphically.
     * Requirements: 17.4, 17.5
     */
    public void dispatchToSenders(Notification notification, NotificationPreferences preferences) {
        ValidationUtils.validateNotBlank(notification, "notification");
        ValidationUtils.validateNotBlank(preferences, "preferences");
        for (NotificationSender sender : senders) {
            if (sender.canSend(notification, preferences)) {
                sender.send(notification);
            }
        }
    }

    /**
     * Dispatches due records in batches (idempotent).
     */
    public int scheduleDelivery(int limit) {
        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException("limit must be in 1..1000");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        List<NotificationDispatchRecord> due = dispatchRepository.findPending(now, limit);
        int processed = 0;
        for (NotificationDispatchRecord record : due) {
            dispatchNotification(record.getNotificationId(), record.getChannel());
            processed++;
        }
        return processed;
    }

    public void retryFailed(int limit) {
        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException("limit must be in 1..1000");
        }
        List<NotificationDispatchRecord> failed = dispatchRepository.findFailed(limit);
        for (NotificationDispatchRecord record : failed) {
            dispatchNotification(record.getNotificationId(), record.getChannel());
        }
    }

    public void dispatchNotification(UUID notificationId, NotificationChannel channel) {
        ValidationUtils.validateUUID(notificationId, "notificationId");
        ValidationUtils.validateNotBlank(channel, "channel");

        NotificationDispatchRecord record = dispatchRepository.findRecord(notificationId, channel)
                .orElseThrow(() -> new IllegalStateException("dispatch record not found"));

        if (record.isTerminal()) {
            return; // idempotent
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("notification not found"));

        NotificationPreferences prefs = preferencesRepository.findByUserId(notification.getUserId())
                .orElseGet(() -> NotificationPreferences.defaults(notification.getUserId()));

        if (channel == NotificationChannel.IN_APP) {
            // Persisted notification already guarantees in-app availability.
            record.markSent(clock);
            dispatchRepository.save(record);
            dispatchRepository.appendLog(new NotificationDispatchLog(UUID.randomUUID(), notificationId, channel, true,
                    null, clock));
            return;
        }

        // EMAIL channel
        if (notification.getPriority() != NotificationPriority.HIGH) {
            record.markSkipped("priority not HIGH", clock);
            dispatchRepository.save(record);
            dispatchRepository.appendLog(new NotificationDispatchLog(UUID.randomUUID(), notificationId, channel, true,
                    null, clock));
            return;
        }

        if (!prefs.isEmailEnabled()) {
            record.markSkipped("email disabled", clock);
            dispatchRepository.save(record);
            dispatchRepository.appendLog(new NotificationDispatchLog(UUID.randomUUID(), notificationId, channel, true,
                    null, clock));
            return;
        }

        try {
            emailGateway.send(notification.getUserId().toString(), notification.getTitle(),
                    notification.getContent());
            record.markSent(clock);
            dispatchRepository.save(record);
            dispatchRepository.appendLog(new NotificationDispatchLog(UUID.randomUUID(), notificationId, channel, true,
                    null, clock));
        } catch (RuntimeException ex) {
            LocalDateTime next = computeNextAttempt(clock, record.getRetryCount());
            record.markFailed(ex.getMessage() == null ? "dispatch failure" : ex.getMessage(), next, clock);
            dispatchRepository.save(record);
            dispatchRepository.appendLog(new NotificationDispatchLog(UUID.randomUUID(), notificationId, channel, false,
                    ex.getMessage() == null ? "dispatch failure" : ex.getMessage(), clock));
        }
    }

    private static LocalDateTime computeNextAttempt(Clock clock, int retryCount) {
        int capped = Math.min(Math.max(retryCount, 0), 10);
        long seconds = (long) Math.min(3600, Math.pow(2, capped) * 5L);
        return LocalDateTime.now(clock).plus(Duration.ofSeconds(seconds));
    }
}

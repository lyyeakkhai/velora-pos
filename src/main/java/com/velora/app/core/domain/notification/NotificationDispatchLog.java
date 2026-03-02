package com.velora.app.core.domain.notification;

import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit log record for a single dispatch attempt.
 */
public class NotificationDispatchLog {

    private final UUID attemptId;
    private final UUID notificationId;
    private final NotificationChannel channel;
    private final boolean success;
    private final String failureReason;
    private final LocalDateTime attemptedAt;

    public NotificationDispatchLog(UUID attemptId, UUID notificationId, NotificationChannel channel, boolean success,
            String failureReason, Clock clock) {
        ValidationUtils.validateUUID(attemptId, "attemptId");
        ValidationUtils.validateUUID(notificationId, "notificationId");
        ValidationUtils.validateNotBlank(channel, "channel");
        if (success) {
            if (failureReason != null) {
                throw new IllegalArgumentException("failureReason must be null when success=true");
            }
        } else {
            ValidationUtils.validateNotBlank(failureReason, "failureReason");
        }

        this.attemptId = attemptId;
        this.notificationId = notificationId;
        this.channel = channel;
        this.success = success;
        this.failureReason = failureReason == null ? null : failureReason.trim();
        this.attemptedAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}

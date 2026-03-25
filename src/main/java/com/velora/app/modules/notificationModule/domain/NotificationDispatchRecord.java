package com.velora.app.modules.notificationModule.domain;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Idempotency and audit state for dispatching a notification in a specific channel.
 */
public class NotificationDispatchRecord extends AbstractAuditableEntity {

    private final UUID notificationId;
    private final NotificationChannel channel;

    private DispatchStatus status;
    private int retryCount;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime nextAttemptAt;
    private String lastFailureReason;

    public NotificationDispatchRecord(UUID notificationId, NotificationChannel channel, LocalDateTime nextAttemptAt) {
        super(UUID.randomUUID());
        ValidationUtils.validateUUID(notificationId, "notificationId");
        ValidationUtils.validateNotBlank(channel, "channel");
        ValidationUtils.validateNotBlank(nextAttemptAt, "nextAttemptAt");
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = DispatchStatus.PENDING;
        this.retryCount = 0;
        this.nextAttemptAt = nextAttemptAt;
    }

    public static NotificationDispatchRecord createNow(UUID notificationId, NotificationChannel channel, Clock clock) {
        return new NotificationDispatchRecord(notificationId, channel,
                LocalDateTime.now(clock == null ? Clock.systemUTC() : clock));
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public DispatchStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    public boolean isTerminal() {
        return status == DispatchStatus.SENT || status == DispatchStatus.SKIPPED;
    }

    public void markSent(Clock clock) {
        this.status = DispatchStatus.SENT;
        this.lastFailureReason = null;
        this.lastAttemptAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
        this.nextAttemptAt = null;
        touch();
    }

    public void markSkipped(String reason, Clock clock) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason required");
        }
        this.status = DispatchStatus.SKIPPED;
        this.lastFailureReason = reason.trim();
        this.lastAttemptAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
        this.nextAttemptAt = null;
        touch();
    }

    public void markFailed(String failureReason, LocalDateTime nextAttemptAt, Clock clock) {
        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("failureReason required");
        }
        ValidationUtils.validateNotBlank(nextAttemptAt, "nextAttemptAt");
        this.status = DispatchStatus.FAILED;
        this.retryCount += 1;
        this.lastFailureReason = failureReason.trim();
        this.lastAttemptAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
        this.nextAttemptAt = nextAttemptAt;
        touch();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationDispatchRecord that = (NotificationDispatchRecord) o;
        return notificationId.equals(that.notificationId) && channel == that.channel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, channel);
    }
}

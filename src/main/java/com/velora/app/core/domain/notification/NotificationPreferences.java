package com.velora.app.core.domain.notification;

import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * User preferences for notification delivery.
 */
public class NotificationPreferences {

    private final UUID userId;

    private boolean emailEnabled;
    private final boolean billingAlerts;
    private boolean marketingAlerts;
    private LocalDateTime updatedAt;

    public NotificationPreferences(UUID userId, Boolean emailEnabled, Boolean marketingAlerts, Clock clock) {
        ValidationUtils.validateUUID(userId, "userId");
        this.userId = userId;
        this.emailEnabled = emailEnabled == null ? true : emailEnabled;
        this.billingAlerts = true;
        this.marketingAlerts = marketingAlerts == null ? false : marketingAlerts;
        this.updatedAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
    }

    public static NotificationPreferences defaults(UUID userId, Clock clock) {
        return new NotificationPreferences(userId, true, false, clock);
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled, Clock clock) {
        this.emailEnabled = emailEnabled;
        touch(clock);
    }

    /**
     * Always true by invariant.
     */
    public boolean isBillingAlerts() {
        return billingAlerts;
    }

    public boolean isMarketingAlerts() {
        return marketingAlerts;
    }

    public void setMarketingAlerts(boolean marketingAlerts, Clock clock) {
        this.marketingAlerts = marketingAlerts;
        touch(clock);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private void touch(Clock clock) {
        this.updatedAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationPreferences that = (NotificationPreferences) o;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

package com.velora.app.core.domain.notification;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * User preferences for notification delivery.
 */
public class NotificationPreferences extends AbstractAuditableEntity {

    private boolean emailEnabled;
    private final boolean billingAlerts;
    private boolean marketingAlerts;

    public NotificationPreferences(UUID userId, Boolean emailEnabled, Boolean marketingAlerts) {
        super(userId);
        ValidationUtils.validateUUID(userId, "userId");
        this.emailEnabled = emailEnabled == null ? true : emailEnabled;
        this.billingAlerts = true;
        this.marketingAlerts = marketingAlerts == null ? false : marketingAlerts;
    }

    public static NotificationPreferences defaults(UUID userId) {
        return new NotificationPreferences(userId, true, false);
    }

    public UUID getUserId() {
        return getId();
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
        touch();
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

    public void setMarketingAlerts(boolean marketingAlerts) {
        this.marketingAlerts = marketingAlerts;
        touch();
    }
}

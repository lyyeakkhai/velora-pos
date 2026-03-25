package com.velora.app.common;

import com.velora.app.modules.notificationModule.domain.Notification;
import com.velora.app.modules.notificationModule.domain.NotificationPreferences;

/**
 * Abstract base for all notification dispatchers.
 *
 * <p>Subclasses implement the channel-specific {@link #send(Notification)} method.
 * The template method {@link #dispatch(Notification, NotificationPreferences)} enforces
 * the eligibility check before any send attempt, guaranteeing that billing alerts
 * are never suppressed regardless of user preferences.
 *
 * <p>Requirement 13: AbstractNotificationDispatcher
 */
public abstract class AbstractNotificationDispatcher {

    /**
     * Channel-specific send implementation.
     * Subclasses must not call this directly — use {@link #dispatch(Notification, NotificationPreferences)}.
     *
     * @param notification the notification to send
     */
    public abstract void send(Notification notification);

    /**
     * Determines whether the notification should be sent given the user's preferences.
     *
     * <p>Billing alerts ({@link NotificationPreferences#isBillingAlerts()}) are always
     * dispatched unconditionally. All other notification types are gated on
     * {@link NotificationPreferences#isEmailEnabled()}.
     *
     * @param n     the notification to evaluate
     * @param prefs the recipient's notification preferences
     * @return {@code true} if the notification should be sent
     */
    public boolean shouldSend(Notification n, NotificationPreferences prefs) {
        if (prefs.isBillingAlerts()) {
            return true;
        }
        return prefs.isEmailEnabled();
    }

    /**
     * Template method: checks eligibility then delegates to {@link #send(Notification)}.
     *
     * <p>This method is {@code final} to guarantee that billing alerts are never
     * suppressed by a subclass override.
     *
     * @param n     the notification to dispatch
     * @param prefs the recipient's notification preferences
     */
    public final void dispatch(Notification n, NotificationPreferences prefs) {
        if (shouldSend(n, prefs)) {
            send(n);
        }
    }
}

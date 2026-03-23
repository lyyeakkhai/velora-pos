package com.velora.app.core.domain.notification;

/**
 * Pluggable notification channel sender.
 * Requirements: 17.1
 */
public interface NotificationSender {

    /**
     * Returns the channel this sender handles.
     */
    NotificationChannel getChannel();

    /**
     * Returns true if this sender should send the given notification based on preferences.
     */
    boolean canSend(Notification notification, NotificationPreferences preferences);

    /**
     * Sends the notification via this channel.
     */
    void send(Notification notification);
}

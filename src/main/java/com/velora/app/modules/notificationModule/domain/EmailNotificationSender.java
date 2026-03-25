package com.velora.app.modules.notificationModule.domain;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Email notification sender. Only sends when priority is HIGH and email is enabled.
 * Requirements: 17.3
 */
public class EmailNotificationSender implements NotificationSender {

    private final EmailGateway emailGateway;

    public EmailNotificationSender(EmailGateway emailGateway) {
        ValidationUtils.validateNotBlank(emailGateway, "emailGateway");
        this.emailGateway = emailGateway;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean canSend(Notification notification, NotificationPreferences preferences) {
        return notification.getPriority() == NotificationPriority.HIGH
                && preferences.isEmailEnabled();
    }

    @Override
    public void send(Notification notification) {
        ValidationUtils.validateNotBlank(notification, "notification");
        emailGateway.send(
                notification.getUserId().toString(),
                notification.getTitle(),
                notification.getContent());
    }
}

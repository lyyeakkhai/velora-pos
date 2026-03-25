package com.velora.app.modules.notificationModule.domain;

import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;

/**
 * In-app notification sender. Always eligible to send.
 * Requirements: 17.2
 */
public class InAppNotificationSender implements NotificationSender {

    private final NotificationDispatchRepository dispatchRepository;
    private final Clock clock;

    public InAppNotificationSender(NotificationDispatchRepository dispatchRepository, Clock clock) {
        ValidationUtils.validateNotBlank(dispatchRepository, "dispatchRepository");
        this.dispatchRepository = dispatchRepository;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public boolean canSend(Notification notification, NotificationPreferences preferences) {
        return true;
    }

    @Override
    public void send(Notification notification) {
        ValidationUtils.validateNotBlank(notification, "notification");
        NotificationDispatchRecord record = NotificationDispatchRecord.createNow(
                notification.getNotificationId(), NotificationChannel.IN_APP, clock);
        dispatchRepository.createIfAbsent(record);
    }
}

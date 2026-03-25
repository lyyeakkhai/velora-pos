package com.velora.app.modules.notificationModule.domain;

import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferencesRepository {
    Optional<NotificationPreferences> findByUserId(UUID userId);

    void save(NotificationPreferences preferences);
}

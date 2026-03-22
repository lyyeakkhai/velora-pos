package com.velora.app.core.domain.notification;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.util.UUID;

/**
 * Business service for user notification preferences.
 */
public class PreferenceService {

    private final NotificationPreferencesRepository repository;
    private final Clock clock;

    public PreferenceService(NotificationPreferencesRepository repository, Clock clock) {
        ValidationUtils.validateNotBlank(repository, "repository");
        this.repository = repository;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public NotificationPreferences getPreferences(Role.RoleName actorRole, boolean systemActor, UUID actorUserId,
            UUID userId) {
        NotificationAccessPolicy.requireReadableInbox(actorRole, systemActor);
        NotificationAccessPolicy.requireUserScope(actorUserId, userId);

        return repository.findByUserId(userId).orElseGet(() -> {
            NotificationPreferences created = NotificationPreferences.defaults(userId);
            repository.save(created);
            return created;
        });
    }

    public NotificationPreferences updatePreferences(Role.RoleName actorRole, boolean systemActor, UUID actorUserId,
            UUID userId, boolean emailEnabled, boolean marketingAlerts) {
        NotificationAccessPolicy.requireReadableInbox(actorRole, systemActor);
        NotificationAccessPolicy.requireUserScope(actorUserId, userId);

        NotificationPreferences preferences = repository.findByUserId(userId)
                .orElse(NotificationPreferences.defaults(userId));
        preferences.setEmailEnabled(emailEnabled);
        preferences.setMarketingAlerts(marketingAlerts);
        // billingAlerts is immutable and always TRUE by invariant.
        repository.save(preferences);
        return preferences;
    }

    public NotificationPreferences resetToDefault(Role.RoleName actorRole, boolean systemActor, UUID actorUserId,
            UUID userId) {
        NotificationAccessPolicy.requireReadableInbox(actorRole, systemActor);
        NotificationAccessPolicy.requireUserScope(actorUserId, userId);

        NotificationPreferences preferences = NotificationPreferences.defaults(userId);
        repository.save(preferences);
        return preferences;
    }
}

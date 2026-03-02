package com.velora.app.core.domain.inventoryeventmanagement;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for event types.
 */
public interface EventTypeStore {
    EventType save(EventType eventType);

    Optional<EventType> findById(UUID eventId);
}

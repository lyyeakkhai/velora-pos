package com.velora.app.modules.event_managementModule.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for event types.
 */
public interface EventTypeStore {
    EventType save(EventType eventType);

    Optional<EventType> findById(UUID eventId);

    List<EventType> findByShopId(UUID shopId);
}

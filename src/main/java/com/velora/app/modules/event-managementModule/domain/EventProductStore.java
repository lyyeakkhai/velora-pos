package com.velora.app.modules.event_managementModule.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for event-product junction records.
 */
public interface EventProductStore {
    boolean existsByEventIdAndProductId(UUID eventId, UUID productId);

    EventProduct save(EventProduct eventProduct);

    Optional<EventProduct> findById(UUID eventProductId);

    List<EventProduct> findByEventId(UUID eventId);

    List<EventProduct> findByProductId(UUID productId);
}

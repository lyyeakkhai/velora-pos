package com.velora.app.core.domain.inventoryeventmanagement;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for event-product junction records.
 */
public interface EventProductStore {
    boolean existsByEventIdAndProductId(UUID eventId, UUID productId);

    EventProduct save(EventProduct eventProduct);

    Optional<EventProduct> findById(UUID eventProductId);
}

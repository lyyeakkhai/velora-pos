package com.velora.app.modules.sale_managementModule.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for deliveries.
 */
public interface DeliveryStore {
    Delivery save(Delivery delivery);

    Optional<Delivery> findByOrderId(UUID orderId);
}

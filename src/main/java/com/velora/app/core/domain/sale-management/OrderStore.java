package com.velora.app.core.domain.salemanagement;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for orders.
 */
public interface OrderStore {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);
}

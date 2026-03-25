package com.velora.app.modules.sale_managementModule.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for orders.
 */
public interface OrderStore {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    List<Order> findByShopId(UUID shopId);
}

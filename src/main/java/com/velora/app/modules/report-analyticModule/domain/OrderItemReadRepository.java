package com.velora.app.core.domain.reportandanalytic;

import java.util.List;
import java.util.UUID;

/**
 * Read port for fetching order items needed for aggregation.
 */
public interface OrderItemReadRepository {

    List<OrderItemFact> findItemsForOrders(UUID shopId, List<UUID> orderIds);
}

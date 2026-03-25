package com.velora.app.modules.report_analyticModule.domain;

import java.util.List;
import java.util.UUID;

/**
 * Read port for fetching order items needed for aggregation.
 */
public interface OrderItemReadRepository {

    List<OrderItemFact> findItemsForOrders(UUID shopId, List<UUID> orderIds);
}


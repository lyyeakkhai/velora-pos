package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Read port for fetching confirmed orders in an aggregation window.
 */
public interface ConfirmedOrderReadRepository {

    List<OrderFact> findConfirmedOrdersForShopAndDate(UUID shopId, LocalDate utcDate);
}


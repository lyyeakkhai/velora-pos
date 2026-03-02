package com.velora.app.core.domain.salemanagement;

import java.util.Map;
import java.util.UUID;

/**
 * Domain service for inventory protection and deduction.
 */
public interface InventoryService {

    /**
     * Performs a fast, non-locking stock check.
     */
    void softCheckStock(UUID shopId, Map<UUID, Integer> productQuantities);

    /**
     * Performs a strict stock deduction (should be transactional).
     */
    void hardDeductStock(UUID shopId, Map<UUID, Integer> productQuantities);

    /**
     * Restores stock (e.g., order cancellation / refund).
     */
    void restoreStock(UUID shopId, Map<UUID, Integer> productQuantities);
}

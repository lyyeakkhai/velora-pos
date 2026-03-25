package com.velora.app.modules.sale_managementModule.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for receipts.
 */
public interface ReceiptStore {
    Receipt save(Receipt receipt);

    Optional<Receipt> findByOrderId(UUID orderId);
}

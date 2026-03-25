package com.velora.app.modules.sale_managementModule.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for payment intents.
 */
public interface PaymentIntentStore {

    boolean existsByBankRefId(String bankRefId);

    PaymentIntent save(PaymentIntent intent);

    /**
     * Fetches intent for update/locking.
     */
    Optional<PaymentIntent> getForUpdate(UUID intentId);

    Optional<PaymentIntent> findByBankRefId(String bankRefId);

    void delete(UUID intentId);
}

package com.velora.app.modules.paymentModule.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for PaymentMethod persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 */
public interface PaymentMethodRepository {

    /**
     * Persists a payment method and returns the saved instance.
     *
     * @param paymentMethod The payment method to save (cannot be null)
     * @return The saved payment method
     */
    PaymentMethod save(PaymentMethod paymentMethod);

    /**
     * Finds a payment method by its UUID.
     *
     * @param id The payment method UUID
     * @return An Optional containing the payment method, or empty if not found
     */
    Optional<PaymentMethod> findById(UUID id);
}

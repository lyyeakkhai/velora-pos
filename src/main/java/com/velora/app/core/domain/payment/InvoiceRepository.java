package com.velora.app.core.domain.payment;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Invoice persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 */
public interface InvoiceRepository {

    /**
     * Persists an invoice and returns the saved instance.
     *
     * @param invoice The invoice to save (cannot be null)
     * @return The saved invoice
     */
    Invoice save(Invoice invoice);

    /**
     * Finds an invoice by its UUID.
     *
     * @param id The invoice UUID
     * @return An Optional containing the invoice, or empty if not found
     */
    Optional<Invoice> findById(UUID id);

    /**
     * Finds an invoice associated with a given transaction.
     *
     * @param transactionId The transaction UUID
     * @return An Optional containing the invoice, or empty if not found
     */
    Optional<Invoice> findByTransactionId(UUID transactionId);
}

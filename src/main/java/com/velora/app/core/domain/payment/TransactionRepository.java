package com.velora.app.core.domain.payment;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Transaction persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 */
public interface TransactionRepository {

    /**
     * Persists a transaction and returns the saved instance.
     *
     * @param transaction The transaction to save (cannot be null)
     * @return The saved transaction
     */
    Transaction save(Transaction transaction);

    /**
     * Finds a transaction by its UUID.
     *
     * @param id The transaction UUID
     * @return An Optional containing the transaction, or empty if not found
     */
    Optional<Transaction> findById(UUID id);

    /**
     * Finds a transaction by its external gateway reference.
     *
     * @param gatewayRef The gateway reference string
     * @return An Optional containing the transaction, or empty if not found
     */
    Optional<Transaction> findByGatewayRef(String gatewayRef);
}

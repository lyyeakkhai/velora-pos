package com.velora.app.infrastructure.db;

import java.util.Optional;
import java.util.UUID;

import com.velora.app.modules.paymentModule.domain.Transaction;
import com.velora.app.modules.paymentModule.domain.TransactionRepository;

/**
 * PostgreSQL implementation of TransactionRepository.
 * Requirements: 14.6
 */
public class PostgresTransactionRepository implements TransactionRepository {

    @Override
    public Transaction save(Transaction transaction) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Transaction> findByGatewayRef(String gatewayRef) {
        // TODO: implement JDBC select by gatewayRef
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

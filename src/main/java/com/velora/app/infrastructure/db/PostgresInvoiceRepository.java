package com.velora.app.infrastructure.db;

import com.velora.app.core.domain.payment.Invoice;
import com.velora.app.core.domain.payment.InvoiceRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of InvoiceRepository.
 * Requirements: 14.6
 */
public class PostgresInvoiceRepository implements InvoiceRepository {

    @Override
    public Invoice save(Invoice invoice) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Invoice> findByTransactionId(UUID transactionId) {
        // TODO: implement JDBC select by transactionId
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

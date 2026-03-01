package com.velora.app.core.domain.payment;

/**
 * Transaction state machine.
 */
public enum TransactionStatus {
    PENDING,
    PAID,
    FAILED
}

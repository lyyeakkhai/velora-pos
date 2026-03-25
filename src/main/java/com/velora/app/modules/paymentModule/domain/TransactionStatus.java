package com.velora.app.modules.paymentModule.domain;

/**
 * Transaction state machine.
 */
public enum TransactionStatus {
    PENDING,
    PAID,
    FAILED
}

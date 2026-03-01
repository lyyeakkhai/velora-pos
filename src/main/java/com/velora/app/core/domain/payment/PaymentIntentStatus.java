package com.velora.app.core.domain.payment;

/**
 * Payment intent state machine.
 */
public enum PaymentIntentStatus {
    PENDING,
    SUCCESS,
    FAILED
}

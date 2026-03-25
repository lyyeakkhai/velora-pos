package com.velora.app.modules.paymentModule.domain;

/**
 * Payment intent state machine.
 */
public enum PaymentIntentStatus {
    PENDING,
    SUCCESS,
    FAILED
}

package com.velora.app.core.domain.payment;

import java.math.BigDecimal;

/**
 * Strategy interface for payment processing.
 *
 * <p>Each implementation handles a specific payment method type
 * (e.g., card-based or QR code).
 *
 * <p>Requirements: 22.1
 */
public interface PaymentProcessor {

    /**
     * Returns the card type this processor supports, or {@code null} for non-card processors
     * (e.g., QR code).
     */
    CardType getSupportedCardType();

    /**
     * Creates a {@link PaymentIntent} for the given transaction.
     *
     * @param transaction the transaction to create an intent for
     * @return a new {@link PaymentIntent}
     */
    PaymentIntent createIntent(Transaction transaction);

    /**
     * Verifies that the gateway reference matches the expected amount.
     *
     * @param gatewayRef the external gateway reference
     * @param amount     the expected transaction amount
     * @return {@code true} if verification succeeds, {@code false} otherwise
     */
    boolean verify(String gatewayRef, BigDecimal amount);
}

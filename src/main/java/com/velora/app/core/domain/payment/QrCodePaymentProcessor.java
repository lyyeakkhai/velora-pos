package com.velora.app.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * {@link PaymentProcessor} implementation for QR-code-based payments.
 *
 * <p>{@link #getSupportedCardType()} returns {@code null} because QR code payments
 * are not tied to a card network.
 *
 * <p>Requirements: 22.3
 */
public class QrCodePaymentProcessor implements PaymentProcessor {

    /**
     * QR code payments are not associated with any card type.
     *
     * @return {@code null}
     */
    @Override
    public CardType getSupportedCardType() {
        return null;
    }

    /**
     * Creates a QR code payment intent that expires in 5 minutes and embeds
     * a stub QR payload derived from the transaction ID.
     */
    @Override
    public PaymentIntent createIntent(Transaction transaction) {
        PaymentIntent intent = new PaymentIntent(
                transaction.getTransactionId(),
                transaction.getTransactionId(), // placeholder methodId
                LocalDateTime.now().plusMinutes(5));
        // Embed a stub QR code payload
        intent.setQrCodeData("QR:" + transaction.getTransactionId());
        return intent;
    }

    /**
     * Stub verification — returns {@code true} when a non-blank gateway reference is provided.
     */
    @Override
    public boolean verify(String gatewayRef, BigDecimal amount) {
        return gatewayRef != null && !gatewayRef.isBlank();
    }
}

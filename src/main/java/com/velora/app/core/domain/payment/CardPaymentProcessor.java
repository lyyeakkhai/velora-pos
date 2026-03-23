package com.velora.app.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * {@link PaymentProcessor} implementation for card-based payments (VISA, MASTERCARD, AMEX).
 *
 * <p>Requirements: 22.2
 */
public class CardPaymentProcessor implements PaymentProcessor {

    /** Card types handled by this processor. */
    private static final Set<CardType> SUPPORTED_TYPES = Set.of(
            CardType.VISA, CardType.MASTERCARD, CardType.AMEX);

    private final CardType cardType;

    /**
     * Creates a processor for the given card type.
     *
     * @param cardType must be one of VISA, MASTERCARD, or AMEX
     * @throws IllegalArgumentException if the card type is not supported
     */
    public CardPaymentProcessor(CardType cardType) {
        if (!SUPPORTED_TYPES.contains(cardType)) {
            throw new IllegalArgumentException("Unsupported card type: " + cardType);
        }
        this.cardType = cardType;
    }

    @Override
    public CardType getSupportedCardType() {
        return cardType;
    }

    /**
     * Creates a card payment intent that expires in 15 minutes.
     */
    @Override
    public PaymentIntent createIntent(Transaction transaction) {
        PaymentIntent intent = new PaymentIntent(
                transaction.getTransactionId(),
                transaction.getTransactionId(), // placeholder methodId
                LocalDateTime.now().plusMinutes(15));
        return intent;
    }

    /**
     * Stub verification — delegates to the card gateway.
     * Returns {@code true} when a non-blank gateway reference is provided.
     */
    @Override
    public boolean verify(String gatewayRef, BigDecimal amount) {
        return gatewayRef != null && !gatewayRef.isBlank();
    }
}

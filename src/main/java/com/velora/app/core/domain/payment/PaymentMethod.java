package com.velora.app.core.domain.payment;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain model for a stored payment method.
 * <p>
 * Maps to PAYMENT_METHODS.
 */
public class PaymentMethod {

    private UUID methodId;
    private String gatewayToken;
    private CardType cardType;
    private String lastFour;
    private LocalDate expiryDate;

    /**
     * Creates a payment method with mandatory fields.
     */
    public PaymentMethod(CardType cardType, String lastFour, LocalDate expiryDate) {
        setMethodId(UUID.randomUUID());
        setCardType(cardType);
        setLastFour(lastFour);
        setExpiryDate(expiryDate);
    }

    public UUID getMethodId() {
        return methodId;
    }

    public String getGatewayToken() {
        return gatewayToken;
    }

    /**
     * Optional/encrypted gateway token (e.g., PSP vault token).
     */
    public void setGatewayToken(String gatewayToken) {
        if (gatewayToken == null) {
            this.gatewayToken = null;
            return;
        }
        ValidationUtils.validateNotBlank(gatewayToken, "gatewayToken");
        this.gatewayToken = gatewayToken;
    }

    public CardType getCardType() {
        return cardType;
    }

    private void setCardType(CardType cardType) {
        ValidationUtils.validateNotBlank(cardType, "cardType");
        this.cardType = cardType;
    }

    public String getLastFour() {
        return lastFour;
    }

    private void setLastFour(String lastFour) {
        ValidationUtils.validateExactDigits(lastFour, 4, "lastFour");
        this.lastFour = lastFour;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        ValidationUtils.validateNotBlank(expiryDate, "expiryDate");
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("expiryDate cannot be in the past");
        }
        this.expiryDate = expiryDate;
    }

    private void setMethodId(UUID methodId) {
        ValidationUtils.validateUUID(methodId, "methodId");
        this.methodId = methodId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentMethod)) {
            return false;
        }
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(methodId, that.methodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodId);
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "methodId=" + methodId +
                ", cardType=" + cardType +
                ", lastFour='" + lastFour + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}

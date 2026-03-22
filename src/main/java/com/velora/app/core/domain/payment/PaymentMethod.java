package com.velora.app.core.domain.payment;

import java.time.LocalDate;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain model for a stored payment method.
 * <p>
 * Maps to PAYMENT_METHODS.
 */
public class PaymentMethod extends AbstractAuditableEntity {

    private String gatewayToken;
    private CardType cardType;
    private String lastFour;
    private LocalDate expiryDate;

    /**
     * Creates a payment method with mandatory fields.
     */
    public PaymentMethod(CardType cardType, String lastFour, LocalDate expiryDate) {
        super(UUID.randomUUID());
        setCardType(cardType);
        setLastFour(lastFour);
        setExpiryDate(expiryDate);
    }

    public UUID getMethodId() {
        return getId();
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

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "methodId=" + getId() +
                ", cardType=" + cardType +
                ", lastFour='" + lastFour + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}

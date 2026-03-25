package com.velora.app.modules.payment.domain;

import java.time.LocalDate;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a stored payment method for a user.
 * 
 * <p>
 * PaymentMethod stores card details securely using gateway tokens.
 * Sensitive card data is never stored - only gateway tokens.
 */
public class PaymentMethod extends AbstractAuditableEntity {

    /**
     * Supported card types
     */
    public enum CardType {
        VISA,
        MASTERCARD,
        AMEX,
        OTHER
    }

    private final UUID userId;
    private final String gatewayToken;
    private final CardType cardType;
    private final String lastFour;
    private final LocalDate expiryDate;
    private boolean isDefault;

    /**
     * Creates a new PaymentMethod.
     *
     * @param methodId     The unique method identifier
     * @param userId       The owner's user ID
     * @param gatewayToken The payment gateway's token (not the actual card number)
     * @param cardType     The type of card
     * @param lastFour     The last 4 digits of the card (for display)
     * @param expiryDate   The card expiry date
     */
    public PaymentMethod(UUID methodId, UUID userId, String gatewayToken,
            CardType cardType, String lastFour, LocalDate expiryDate) {
        super(methodId);
        ValidationUtils.validateUUID(userId, "userId");
        ValidationUtils.validateNotBlank(gatewayToken, "gatewayToken");
        ValidationUtils.validateNotBlank(cardType, "cardType");
        ValidationUtils.validateExactDigits(lastFour, 4, "lastFour");
        ValidationUtils.validateNotBlank(expiryDate, "expiryDate");

        this.userId = userId;
        this.gatewayToken = gatewayToken;
        this.cardType = cardType;
        this.lastFour = lastFour;
        this.expiryDate = expiryDate;
        this.isDefault = false;
    }

    /**
     * Gets the user identifier.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Gets the gateway token (not the actual card number).
     */
    public String getGatewayToken() {
        return gatewayToken;
    }

    /**
     * Gets the card type.
     */
    public CardType getCardType() {
        return cardType;
    }

    /**
     * Gets the last 4 digits.
     */
    public String getLastFour() {
        return lastFour;
    }

    /**
     * Gets the expiry date.
     */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Checks if this is the default payment method.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Sets as the default payment method.
     */
    public void setAsDefault() {
        this.isDefault = true;
        touch();
    }

    /**
     * Removes default status.
     */
    public void removeDefault() {
        this.isDefault = false;
        touch();
    }

    /**
     * Checks if the card is expired.
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Checks if the card expires within the given number of months.
     *
     * @param months Number of months to check
     * @return true if card expires within the given months
     */
    public boolean expiresWithin(int months) {
        LocalDate threshold = LocalDate.now().plusMonths(months);
        return !expiryDate.isAfter(threshold);
    }

    /**
     * Gets a display-friendly description of the card.
     *
     * @return e.g., "Visa ending in 1234"
     */
    public String getDisplayName() {
        return String.format("%s ending in %s", cardType.name(), lastFour);
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id=" + getId() +
                ", userId=" + userId +
                ", cardType=" + cardType +
                ", lastFour='" + lastFour + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}

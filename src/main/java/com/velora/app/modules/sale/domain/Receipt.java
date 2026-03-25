package com.velora.app.modules.sale.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a payment receipt for an order.
 * 
 * <p>
 * Receipt is immutable once issued and provides proof of payment.
 * Receipt numbers follow the format INV-XXXX for easy identification.
 */
public class Receipt extends AbstractAuditableEntity {

    private final UUID orderId;
    private final String receiptNumber;
    private boolean isPaid;
    private String bankTransactionRef;
    private final LocalDateTime issuedAt;

    /**
     * Creates a new Receipt in UNPAID status.
     *
     * @param receiptId     The unique receipt identifier
     * @param orderId       The associated order identifier
     * @param receiptNumber The formatted receipt number (INV-XXXX)
     */
    public Receipt(UUID receiptId, UUID orderId, String receiptNumber) {
        super(receiptId);
        ValidationUtils.validateUUID(orderId, "orderId");
        ValidationUtils.validateReceiptNumber(receiptNumber, "receiptNumber");

        this.orderId = orderId;
        this.receiptNumber = receiptNumber;
        this.isPaid = false;
        this.bankTransactionRef = null;
        this.issuedAt = LocalDateTime.now();
    }

    /**
     * Gets the order identifier.
     */
    public UUID getOrderId() {
        return orderId;
    }

    /**
     * Gets the formatted receipt number.
     */
    public String getReceiptNumber() {
        return receiptNumber;
    }

    /**
     * Checks if payment has been confirmed.
     */
    public boolean isPaid() {
        return isPaid;
    }

    /**
     * Gets the bank transaction reference.
     */
    public String getBankTransactionRef() {
        return bankTransactionRef;
    }

    /**
     * Gets the issuance timestamp.
     */
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    /**
     * Confirms payment with bank transaction reference.
     *
     * @param bankRef The bank's transaction reference (required)
     * @throws IllegalStateException    if already paid
     * @throws IllegalArgumentException if bankRef is null or blank
     */
    public void confirmPayment(String bankRef) {
        if (isPaid) {
            throw new IllegalStateException("Receipt is already paid");
        }
        ValidationUtils.validateNotBlank(bankRef, "bankTransactionRef");

        this.isPaid = true;
        this.bankTransactionRef = bankRef.trim();
        touch();
    }

    /**
     * Generates the next receipt number in sequence.
     * Format: INV-XXXX where X is a digit.
     *
     * @param sequence The sequence number
     * @return The formatted receipt number
     */
    public static String generateReceiptNumber(int sequence) {
        return String.format("INV-%04d", sequence % 10000);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "id=" + getId() +
                ", orderId=" + orderId +
                ", receiptNumber='" + receiptNumber + '\'' +
                ", isPaid=" + isPaid +
                ", bankTransactionRef='" + bankTransactionRef + '\'' +
                '}';
    }
}

package com.velora.app.modules.sale_managementModule.domain;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Immutable receipt record for an order.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 * createdAt serves as the issuedAt timestamp.
 */
public class Receipt extends AbstractAuditableEntity {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private UUID orderId;
    private String receiptNumber;
    private boolean paid;
    private String bankTransactionRef;

    /**
     * Creates a receipt for an order. id/createdAt are managed by the base class.
     */
    public Receipt(UUID orderId) {
        super(UUID.randomUUID());
        setOrderId(orderId);
        setReceiptNumber(generateNumber());
        setPaid(false);
        setBankTransactionRef(null);
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public boolean isPaid() {
        return paid;
    }

    public String getBankTransactionRef() {
        return bankTransactionRef;
    }

    /**
     * Confirms payment for this receipt.
     * <p>
     * Idempotent: calling again with the same bankRef is allowed; with a different
     * bankRef is rejected.
     */
    public void confirmPayment(String bankRef) {
        ValidationUtils.validateNotBlank(bankRef, "bankTransactionRef");
        if (paid) {
            if (Objects.equals(this.bankTransactionRef, bankRef)) {
                return;
            }
            throw new IllegalStateException("Receipt already paid with a different bank reference");
        }
        setBankTransactionRef(bankRef);
        setPaid(true);
        touch();
    }

    /**
     * Generates a receipt number in the format INV-XXXX.
     */
    public String generateNumber() {
        int n = SECURE_RANDOM.nextInt(10000);
        String number = String.format("INV-%04d", n);
        ValidationUtils.validateReceiptNumber(number, "receiptNumber");
        return number;
    }

    private void setOrderId(UUID orderId) {
        ValidationUtils.validateUUID(orderId, "orderId");
        this.orderId = orderId;
    }

    private void setReceiptNumber(String receiptNumber) {
        ValidationUtils.validateReceiptNumber(receiptNumber, "receiptNumber");
        this.receiptNumber = receiptNumber;
    }

    private void setPaid(boolean paid) {
        this.paid = paid;
    }

    private void setBankTransactionRef(String bankTransactionRef) {
        if (bankTransactionRef == null) {
            this.bankTransactionRef = null;
            return;
        }
        ValidationUtils.validateNotBlank(bankTransactionRef, "bankTransactionRef");
        this.bankTransactionRef = bankTransactionRef;
    }

    @Override
    public String toString() {
        return "Receipt{id=" + getId() +
                ", orderId=" + orderId +
                ", receiptNumber='" + receiptNumber + '\'' +
                ", paid=" + paid +
                ", issuedAt=" + getCreatedAt() +
                '}';
    }
}

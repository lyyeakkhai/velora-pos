package com.velora.app.core.domain.salemanagement;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Immutable receipt record for an order.
 */
public class Receipt {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private UUID receiptId;
    private UUID orderId;
    private String receiptNumber;
    private boolean paid;
    private String bankTransactionRef;
    private LocalDateTime issuedAt;

    /**
     * Creates a receipt for an order. receiptId/issuedAt are generated.
     */
    public Receipt(UUID orderId) {
        setReceiptId(UUID.randomUUID());
        setOrderId(orderId);
        setIssuedAt(LocalDateTime.now());
        setReceiptNumber(generateNumber());
        setPaid(false);
        setBankTransactionRef(null);
    }

    public UUID getReceiptId() {
        return receiptId;
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

    public LocalDateTime getIssuedAt() {
        return issuedAt;
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

    private void setReceiptId(UUID receiptId) {
        ValidationUtils.validateUUID(receiptId, "receiptId");
        this.receiptId = receiptId;
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

    private void setIssuedAt(LocalDateTime issuedAt) {
        ValidationUtils.validateNotBlank(issuedAt, "issuedAt");
        this.issuedAt = issuedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Receipt)) {
            return false;
        }
        Receipt receipt = (Receipt) o;
        return Objects.equals(receiptId, receipt.receiptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "receiptId=" + receiptId +
                ", orderId=" + orderId +
                ", receiptNumber='" + receiptNumber + '\'' +
                ", paid=" + paid +
                ", issuedAt=" + issuedAt +
                '}';
    }
}

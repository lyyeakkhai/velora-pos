package com.velora.app.modules.payment.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a billing invoice for a transaction.
 * 
 * <p>
 * Invoice provides the billing document with price breakdown and verification.
 * 
 * <p>
 * Invariant: subTotal + taxAmount - discountPrice == totalAmount
 */
public class Invoice extends AbstractAuditableEntity {

    /**
     * Invoice status lifecycle states
     */
    public enum Status {
        ISSUED, // Invoice issued
        CANCELLED // Invoice cancelled
    }

    private final UUID transactionId;
    private final String invoiceNo;
    private Status status;
    private final BigDecimal subTotal;
    private final BigDecimal taxAmount;
    private final BigDecimal discountPrice;
    private final BigDecimal totalAmount;

    /**
     * Creates a new Invoice in ISSUED status.
     *
     * @param invoiceId     The unique invoice identifier
     * @param invoiceNo     The human-readable invoice number
     * @param transactionId The associated transaction identifier
     * @param subTotal      The subtotal before tax and discount
     * @param taxAmount     The tax amount
     * @param discountPrice The discount amount
     * @param totalAmount   The final total
     */
    public Invoice(UUID invoiceId, String invoiceNo, UUID transactionId,
            BigDecimal subTotal, BigDecimal taxAmount,
            BigDecimal discountPrice, BigDecimal totalAmount) {
        super(invoiceId);
        ValidationUtils.validateNotBlank(invoiceNo, "invoiceNo");
        ValidationUtils.validateUUID(transactionId, "transactionId");
        ValidationUtils.validateNotBlank(subTotal, "subTotal");
        ValidationUtils.validateNotBlank(taxAmount, "taxAmount");
        ValidationUtils.validateNotBlank(discountPrice, "discountPrice");
        ValidationUtils.validateNotBlank(totalAmount, "totalAmount");

        this.transactionId = transactionId;
        this.invoiceNo = invoiceNo;
        this.status = Status.ISSUED;
        this.subTotal = ValidationUtils.normalizeMoney(subTotal, "subTotal");
        this.taxAmount = ValidationUtils.normalizeMoney(taxAmount, "taxAmount");
        this.discountPrice = ValidationUtils.normalizeMoney(discountPrice, "discountPrice");
        this.totalAmount = ValidationUtils.normalizeMoney(totalAmount, "totalAmount");

        // Verify the invoice totals
        verifyTotal();
    }

    /**
     * Gets the transaction identifier.
     */
    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * Gets the invoice number.
     */
    public String getInvoiceNo() {
        return invoiceNo;
    }

    /**
     * Gets the current status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the subtotal.
     */
    public BigDecimal getSubTotal() {
        return subTotal;
    }

    /**
     * Gets the tax amount.
     */
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    /**
     * Gets the discount price.
     */
    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    /**
     * Gets the total amount.
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Checks if the invoice is issued.
     */
    public boolean isIssued() {
        return status == Status.ISSUED;
    }

    /**
     * Checks if the invoice is cancelled.
     */
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    /**
     * Cancels the invoice.
     *
     * @throws IllegalStateException if already cancelled
     */
    public void cancel() {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("Invoice is already cancelled");
        }
        this.status = Status.CANCELLED;
        touch();
    }

    /**
     * Verifies that the total calculation is correct.
     * Formula: subTotal + taxAmount - discountPrice == totalAmount
     *
     * @throws IllegalStateException if totals don't match
     */
    public void verifyTotal() {
        BigDecimal calculatedTotal = subTotal
                .add(taxAmount)
                .subtract(discountPrice);

        if (calculatedTotal.compareTo(totalAmount) != 0) {
            throw new IllegalStateException(
                    String.format(
                            "Invoice total mismatch. Expected: %s, Calculated: %s (subTotal=%s + tax=%s - discount=%s)",
                            totalAmount, calculatedTotal, subTotal, taxAmount, discountPrice));
        }
    }

    /**
     * Calculates the expected total from components.
     *
     * @param subTotal      The subtotal
     * @param taxAmount     The tax amount
     * @param discountPrice The discount amount
     * @return The calculated total
     */
    public static BigDecimal calculateTotal(BigDecimal subTotal,
            BigDecimal taxAmount,
            BigDecimal discountPrice) {
        return subTotal.add(taxAmount).subtract(discountPrice)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + getId() +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

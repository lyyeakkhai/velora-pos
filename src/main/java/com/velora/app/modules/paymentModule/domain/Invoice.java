package com.velora.app.modules.paymentModule.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.common.DomainException;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain model for invoices.
 * <p>
 * Maps to INVOICES.
 */
public class Invoice extends AbstractAuditableEntity {

    private String invoiceNo;
    private UUID transactionId;
    private InvoiceStatus status;
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal discountPrice;

    public Invoice(String invoiceNo, UUID transactionId, BigDecimal subTotal, BigDecimal taxAmount,
            BigDecimal totalAmount) {
        super(UUID.randomUUID());
        setInvoiceNo(invoiceNo);
        setTransactionId(transactionId);
        setSubTotal(subTotal);
        setTaxAmount(taxAmount);
        setDiscountPrice(BigDecimal.ZERO);
        setTotalAmount(totalAmount);
        setStatus(InvoiceStatus.ISSUED);
        verifyTotal();
    }

    public UUID getInvoiceId() {
        return getId();
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    private void setInvoiceNo(String invoiceNo) {
        ValidationUtils.validateNotBlank(invoiceNo, "invoiceNo");
        this.invoiceNo = invoiceNo;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    private void setTransactionId(UUID transactionId) {
        ValidationUtils.validateUUID(transactionId, "transactionId");
        this.transactionId = transactionId;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = ValidationUtils.normalizeMoney(subTotal, "subTotal");
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = ValidationUtils.normalizeMoney(taxAmount, "taxAmount");
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = ValidationUtils.normalizeMoney(totalAmount, "totalAmount");
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    /**
     * Optional discount amount. Defaults to 0.
     */
    public void setDiscountPrice(BigDecimal discountPrice) {
        if (discountPrice == null) {
            this.discountPrice = BigDecimal.ZERO.setScale(2);
            return;
        }
        this.discountPrice = ValidationUtils.normalizeMoney(discountPrice, "discountPrice");
    }

    /**
     * Ensures sub_total + tax_amount - discount_price == total_amount.
     */
    public void verifyTotal() {
        BigDecimal computed = subTotal.add(taxAmount).subtract(discountPrice);
        computed = computed.setScale(2);
        if (computed.compareTo(totalAmount) != 0) {
            throw new DomainException("Invoice totals mismatch: expected " + computed + " but was " + totalAmount);
        }
    }

    /**
     * Cancels the invoice.
     */
    public void cancel() {
        if (status != InvoiceStatus.ISSUED) {
            throw new DomainException("Only ISSUED invoices can be cancelled");
        }
        setStatus(InvoiceStatus.CANCELLED);
    }

    private void setStatus(InvoiceStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + getId() +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", transactionId=" + transactionId +
                ", status=" + status +
                ", subTotal=" + subTotal +
                ", taxAmount=" + taxAmount +
                ", discountPrice=" + discountPrice +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

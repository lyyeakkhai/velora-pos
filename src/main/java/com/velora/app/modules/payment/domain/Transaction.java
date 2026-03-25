package com.velora.app.modules.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a financial transaction in the platform.
 * 
 * <p>
 * Transaction tracks payment lifecycle for subscription billing and order
 * payments.
 * 
 * <p>
 * State transitions:
 * <ul>
 * <li>PENDING → PAID (successful payment)</li>
 * <li>PENDING → FAILED (payment failed)</li>
 * </ul>
 */
public class Transaction extends AbstractAuditableEntity {

    /**
     * Transaction status lifecycle states
     */
    public enum Status {
        PENDING, // Awaiting payment confirmation
        PAID, // Payment successful
        FAILED // Payment failed (terminal)
    }

    /**
     * Payer type for the transaction
     */
    public enum PayerType {
        USER, // Paid by individual user
        SHOP // Paid by shop account
    }

    private final UUID transactionId;
    private final BigDecimal amount;
    private final String currency;
    private final PayerType payerType;
    private final UUID payerId;
    private final UUID planId; // For subscription transactions
    private final UUID orderId; // For order transactions
    private Status status;
    private String gatewayRef; // Immutable once set
    private LocalDateTime paidAt;

    /**
     * Creates a new Transaction in PENDING status.
     *
     * @param transactionId The unique transaction identifier
     * @param amount        The transaction amount
     * @param currency      The currency code (e.g., "USD", "KHR")
     * @param payerType     Who is paying
     * @param payerId       The payer's identifier
     * @param planId        The plan ID (for subscription payments, may be null)
     * @param orderId       The order ID (for order payments, may be null)
     */
    public Transaction(UUID transactionId, BigDecimal amount, String currency,
            PayerType payerType, UUID payerId, UUID planId, UUID orderId) {
        super(transactionId);
        ValidationUtils.validateNotBlank(amount, "amount");
        ValidationUtils.validateNotBlank(currency, "currency");
        ValidationUtils.validateNotBlank(payerType, "payerType");
        ValidationUtils.validateUUID(payerId, "payerId");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }

        this.transactionId = transactionId;
        this.amount = ValidationUtils.normalizeMoney(amount, "amount");
        this.currency = currency.toUpperCase();
        this.payerType = payerType;
        this.payerId = payerId;
        this.planId = planId;
        this.orderId = orderId;
        this.status = Status.PENDING;
        this.gatewayRef = null;
        this.paidAt = null;
    }

    /**
     * Gets the transaction ID.
     */
    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * Gets the amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Gets the currency code.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Gets the payer type.
     */
    public PayerType getPayerType() {
        return payerType;
    }

    /**
     * Gets the payer ID.
     */
    public UUID getPayerId() {
        return payerId;
    }

    /**
     * Gets the plan ID (for subscription payments).
     */
    public UUID getPlanId() {
        return planId;
    }

    /**
     * Gets the order ID (for order payments).
     */
    public UUID getOrderId() {
        return orderId;
    }

    /**
     * Gets the current status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the gateway reference.
     */
    public String getGatewayRef() {
        return gatewayRef;
    }

    /**
     * Gets the payment timestamp.
     */
    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    /**
     * Checks if this is a subscription transaction.
     */
    public boolean isSubscriptionTransaction() {
        return planId != null;
    }

    /**
     * Checks if this is an order transaction.
     */
    public boolean isOrderTransaction() {
        return orderId != null;
    }

    /**
     * Checks if the transaction is paid.
     */
    public boolean isPaid() {
        return status == Status.PAID;
    }

    /**
     * Checks if the transaction is pending.
     */
    public boolean isPending() {
        return status == Status.PENDING;
    }

    /**
     * Marks the transaction as paid with gateway reference.
     *
     * @param gatewayRef The payment gateway's reference ID
     * @throws IllegalStateException    if not in PENDING status
     * @throws IllegalArgumentException if gatewayRef is null or blank
     */
    public void markPaid(String gatewayRef) {
        if (status != Status.PENDING) {
            throw new IllegalStateException(
                    "Transaction can only be marked paid from PENDING status. Current: " + status);
        }
        ValidationUtils.validateNotBlank(gatewayRef, "gatewayRef");

        this.status = Status.PAID;
        this.gatewayRef = gatewayRef;
        this.paidAt = LocalDateTime.now();
        touch();
    }

    /**
     * Marks the transaction as failed.
     *
     * @throws IllegalStateException if not in PENDING status
     */
    public void markFailed() {
        if (status != Status.PENDING) {
            throw new IllegalStateException(
                    "Transaction can only be failed from PENDING status. Current: " + status);
        }
        this.status = Status.FAILED;
        touch();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + getId() +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", payerType=" + payerType +
                ", status=" + status +
                '}';
    }
}

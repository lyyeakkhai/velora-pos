package com.velora.app.core.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.common.DomainException;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain model for a transaction.
 * <p>
 * Maps to TRANSACTIONS.
 */
public class Transaction {

    private UUID transactionId;
    private BigDecimal amount;
    private Currency currency;
    private PayerType payerType;
    private UUID payerId;
    private UUID planId;
    private String gatewayRef;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    /**
     * Creates a transaction with mandatory fields. Status defaults to PENDING and
     * createdAt is auto-set.
     */
    public Transaction(BigDecimal amount, Currency currency, PayerType payerType, UUID payerId) {
        setTransactionId(UUID.randomUUID());
        setAmount(amount);
        setCurrency(currency);
        setPayerType(payerType);
        setPayerId(payerId);
        setStatus(TransactionStatus.PENDING);
        setCreatedAt(LocalDateTime.now());
        setPaidAt(null);
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public PayerType getPayerType() {
        return payerType;
    }

    public UUID getPayerId() {
        return payerId;
    }

    public UUID getPlanId() {
        return planId;
    }

    /**
     * Optional plan identifier.
     */
    public void setPlanId(UUID planId) {
        if (planId == null) {
            this.planId = null;
            return;
        }
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    public String getGatewayRef() {
        return gatewayRef;
    }

    /**
     * Optional unique gateway reference. Once set, it cannot be changed.
     */
    public void setGatewayRef(String gatewayRef) {
        if (this.gatewayRef != null && !this.gatewayRef.equals(gatewayRef)) {
            throw new DomainException("gatewayRef cannot be changed once set");
        }
        if (gatewayRef != null) {
            ValidationUtils.validateNotBlank(gatewayRef, "gatewayRef");
        }
        this.gatewayRef = gatewayRef;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    /**
     * Marks this transaction as paid, setting paidAt.
     */
    public void markPaid() {
        requireStatus(TransactionStatus.PENDING);
        setStatus(TransactionStatus.PAID);
        setPaidAt(LocalDateTime.now());
    }

    /**
     * Marks this transaction as failed.
     */
    public void markFailed() {
        requireStatus(TransactionStatus.PENDING);
        setStatus(TransactionStatus.FAILED);
        setPaidAt(null);
    }

    private void requireStatus(TransactionStatus expected) {
        if (status != expected) {
            throw new DomainException("Illegal transaction transition from " + status);
        }
    }

    private void setTransactionId(UUID transactionId) {
        ValidationUtils.validateUUID(transactionId, "transactionId");
        this.transactionId = transactionId;
    }

    private void setAmount(BigDecimal amount) {
        this.amount = ValidationUtils.normalizeMoney(amount, "amount");
    }

    private void setCurrency(Currency currency) {
        ValidationUtils.validateNotBlank(currency, "currency");
        this.currency = currency;
    }

    private void setPayerType(PayerType payerType) {
        ValidationUtils.validateNotBlank(payerType, "payerType");
        this.payerType = payerType;
    }

    private void setPayerId(UUID payerId) {
        ValidationUtils.validateUUID(payerId, "payerId");
        this.payerId = payerId;
    }

    private void setStatus(TransactionStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setCreatedAt(LocalDateTime createdAt) {
        ValidationUtils.validateNotBlank(createdAt, "createdAt");
        this.createdAt = createdAt;
    }

    private void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction)) {
            return false;
        }
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", amount=" + amount +
                ", currency=" + currency +
                ", payerType=" + payerType +
                ", payerId=" + payerId +
                ", planId=" + planId +
                ", gatewayRef='" + gatewayRef + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", paidAt=" + paidAt +
                '}';
    }
}

package com.velora.app.modules.paymentModule.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.common.DomainException;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain model for a transaction.
 * <p>
 * Maps to TRANSACTIONS.
 */
public class Transaction extends AbstractAuditableEntity {

    private BigDecimal amount;
    private Currency currency;
    private PayerType payerType;
    private UUID payerId;
    private UUID planId;
    private String gatewayRef;
    private TransactionStatus status;
    private LocalDateTime paidAt;

    /**
     * Creates a transaction with mandatory fields. Status defaults to PENDING and
     * createdAt is auto-set via AbstractAuditableEntity.
     */
    public Transaction(BigDecimal amount, Currency currency, PayerType payerType, UUID payerId) {
        super(UUID.randomUUID());
        setAmount(amount);
        setCurrency(currency);
        setPayerType(payerType);
        setPayerId(payerId);
        setStatus(TransactionStatus.PENDING);
        this.paidAt = null;
    }

    public UUID getTransactionId() {
        return getId();
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
        ValidationUtils.validateNotBlank(gatewayRef, "gatewayRef");
        if (this.gatewayRef != null && !this.gatewayRef.equals(gatewayRef)) {
            throw new DomainException("gatewayRef cannot be changed once set");
        }
        this.gatewayRef = gatewayRef;
    }

    public TransactionStatus getStatus() {
        return status;
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
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Marks this transaction as failed.
     */
    public void markFailed() {
        requireStatus(TransactionStatus.PENDING);
        setStatus(TransactionStatus.FAILED);
        this.paidAt = null;
    }

    private void requireStatus(TransactionStatus expected) {
        if (status != expected) {
            throw new DomainException("Illegal transaction transition from " + status);
        }
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

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + getId() +
                ", amount=" + amount +
                ", currency=" + currency +
                ", payerType=" + payerType +
                ", payerId=" + payerId +
                ", planId=" + planId +
                ", gatewayRef='" + gatewayRef + '\'' +
                ", status=" + status +
                ", createdAt=" + getCreatedAt() +
                ", paidAt=" + paidAt +
                '}';
    }
}

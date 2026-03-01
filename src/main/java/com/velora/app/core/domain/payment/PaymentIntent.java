package com.velora.app.core.domain.payment;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.common.DomainException;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain model for a payment intent.
 * <p>
 * Maps to PAYMENT_INTENTS.
 */
public class PaymentIntent {

    private UUID intentId;
    private UUID transactionId;
    private String qrCodeData;
    private PaymentIntentStatus status;
    private LocalDateTime expiresAt;
    private UUID methodId;

    /**
     * Creates an intent with mandatory fields. Status defaults to PENDING.
     */
    public PaymentIntent(UUID transactionId, UUID methodId, LocalDateTime expiresAt) {
        setIntentId(UUID.randomUUID());
        setTransactionId(transactionId);
        setMethodId(methodId);
        setExpiresAt(expiresAt);
        setStatus(PaymentIntentStatus.PENDING);
    }

    public UUID getIntentId() {
        return intentId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    private void setTransactionId(UUID transactionId) {
        ValidationUtils.validateUUID(transactionId, "transactionId");
        this.transactionId = transactionId;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        if (qrCodeData == null) {
            this.qrCodeData = null;
            return;
        }
        ValidationUtils.validateNotBlank(qrCodeData, "qrCodeData");
        this.qrCodeData = qrCodeData;
    }

    public PaymentIntentStatus getStatus() {
        return status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        ValidationUtils.validateNotBlank(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }
        this.expiresAt = expiresAt;
    }

    public UUID getMethodId() {
        return methodId;
    }

    private void setMethodId(UUID methodId) {
        ValidationUtils.validateUUID(methodId, "methodId");
        this.methodId = methodId;
    }

    /**
     * Marks this intent as successful if it is currently pending and not expired.
     */
    public void markSuccess() {
        requirePending();
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new DomainException("Cannot mark success: payment intent is expired");
        }
        setStatus(PaymentIntentStatus.SUCCESS);
    }

    /**
     * Marks this intent as failed if it is currently pending.
     */
    public void markFailed() {
        requirePending();
        setStatus(PaymentIntentStatus.FAILED);
    }

    private void requirePending() {
        if (status != PaymentIntentStatus.PENDING) {
            throw new DomainException("Illegal payment intent transition from " + status);
        }
    }

    private void setIntentId(UUID intentId) {
        ValidationUtils.validateUUID(intentId, "intentId");
        this.intentId = intentId;
    }

    private void setStatus(PaymentIntentStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentIntent)) {
            return false;
        }
        PaymentIntent that = (PaymentIntent) o;
        return Objects.equals(intentId, that.intentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intentId);
    }

    @Override
    public String toString() {
        return "PaymentIntent{" +
                "intentId=" + intentId +
                ", transactionId=" + transactionId +
                ", status=" + status +
                ", expiresAt=" + expiresAt +
                ", methodId=" + methodId +
                '}';
    }
}

package com.velora.app.modules.paymentModule.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import com.velora.app.modules.paymentModule.domain.CardType;
import com.velora.app.modules.paymentModule.domain.Invoice;
import com.velora.app.modules.paymentModule.domain.PayerType;
import com.velora.app.modules.paymentModule.domain.PaymentMethod;
import com.velora.app.modules.paymentModule.domain.PlatformRevenueSnapshot;
import com.velora.app.modules.paymentModule.domain.Transaction;

/**
 * Application-layer contract for payment transactions, invoices, and revenue snapshots.
 *
 * <p>Requirement: 16.1, 16.7
 */
public interface IPaymentService {

    /**
     * Creates a new PENDING transaction.
     */
    Transaction createTransaction(BigDecimal amount, Currency currency, PayerType payerType, UUID payerId);

    /**
     * Marks a transaction as PAID and records the gateway reference.
     */
    Transaction markTransactionPaid(UUID transactionId, String gatewayRef);

    /**
     * Marks a transaction as FAILED.
     */
    Transaction markTransactionFailed(UUID transactionId);

    /**
     * Issues an invoice linked to a transaction.
     */
    Invoice issueInvoice(String invoiceNo, UUID transactionId, BigDecimal subTotal,
            BigDecimal taxAmount, BigDecimal totalAmount);

    /**
     * Cancels an issued invoice.
     */
    Invoice cancelInvoice(UUID invoiceId);

    /**
     * Registers a new payment method.
     */
    PaymentMethod registerPaymentMethod(CardType cardType, String lastFour, LocalDate expiryDate);

    /**
     * Generates the daily platform revenue snapshot for the given date.
     */
    PlatformRevenueSnapshot generateDailySnapshot(LocalDate snapshotDate);
}

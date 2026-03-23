package com.velora.app.core.service.payment;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.payment.CardType;
import com.velora.app.core.domain.payment.Invoice;
import com.velora.app.core.domain.payment.InvoiceRepository;
import com.velora.app.core.domain.payment.PayerType;
import com.velora.app.core.domain.payment.PaymentIntent;
import com.velora.app.core.domain.payment.PaymentMethod;
import com.velora.app.core.domain.payment.PaymentMethodRepository;
import com.velora.app.core.domain.payment.PaymentProcessor;
import com.velora.app.core.domain.payment.PlatformRevenueSnapshot;
import com.velora.app.core.domain.payment.PlatformRevenueSnapshotRepository;
import com.velora.app.core.domain.payment.Transaction;
import com.velora.app.core.domain.payment.TransactionRepository;
import com.velora.app.core.service.IPaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application-layer service for payment transactions, invoices, and revenue snapshots.
 *
 * <p>Extends {@link AbstractDomainService} to reuse {@code requireNotNull} guard methods.
 * Delegates domain logic to the payment domain entities.
 *
 * <p>Requirements: 16.7, 22.4, 22.5
 */
public class PaymentService extends AbstractDomainService implements IPaymentService {

    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PlatformRevenueSnapshotRepository platformRevenueSnapshotRepository;
    private final List<PaymentProcessor> processors;

    public PaymentService(
            TransactionRepository transactionRepository,
            InvoiceRepository invoiceRepository,
            PaymentMethodRepository paymentMethodRepository,
            PlatformRevenueSnapshotRepository platformRevenueSnapshotRepository,
            List<PaymentProcessor> processors) {
        this.transactionRepository = transactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.platformRevenueSnapshotRepository = platformRevenueSnapshotRepository;
        this.processors = processors != null ? processors : List.of();
    }

    /**
     * Creates a new PENDING transaction.
     *
     * @param amount    the transaction amount
     * @param currency  the currency
     * @param payerType the type of payer (USER or SHOP)
     * @param payerId   the UUID of the payer
     * @return the persisted {@link Transaction}
     */
    @Override
    public Transaction createTransaction(BigDecimal amount, Currency currency, PayerType payerType, UUID payerId) {
        requireNotNull(amount, "amount");
        requireNotNull(currency, "currency");
        requireNotNull(payerType, "payerType");
        requireNotNull(payerId, "payerId");

        Transaction transaction = new Transaction(amount, currency, payerType, payerId);
        return transactionRepository.save(transaction);
    }

    /**
     * Marks a transaction as PAID and records the gateway reference.
     *
     * @param transactionId the UUID of the transaction
     * @param gatewayRef    the external gateway reference string
     * @return the updated {@link Transaction}
     * @throws DomainException if the transaction is not found
     */
    @Override
    public Transaction markTransactionPaid(UUID transactionId, String gatewayRef) {
        requireNotNull(transactionId, "transactionId");
        requireNotNull(gatewayRef, "gatewayRef");

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new DomainException("Transaction not found: " + transactionId));

        transaction.setGatewayRef(gatewayRef);
        transaction.markPaid();
        return transactionRepository.save(transaction);
    }

    /**
     * Marks a transaction as FAILED.
     *
     * @param transactionId the UUID of the transaction
     * @return the updated {@link Transaction}
     * @throws DomainException if the transaction is not found
     */
    @Override
    public Transaction markTransactionFailed(UUID transactionId) {
        requireNotNull(transactionId, "transactionId");

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new DomainException("Transaction not found: " + transactionId));

        transaction.markFailed();
        return transactionRepository.save(transaction);
    }

    /**
     * Issues an invoice linked to a transaction.
     *
     * @param invoiceNo     the invoice number
     * @param transactionId the UUID of the linked transaction
     * @param subTotal      the subtotal amount
     * @param taxAmount     the tax amount
     * @param totalAmount   the total amount
     * @return the persisted {@link Invoice}
     * @throws DomainException if the transaction is not found
     */
    @Override
    public Invoice issueInvoice(String invoiceNo, UUID transactionId, BigDecimal subTotal,
            BigDecimal taxAmount, BigDecimal totalAmount) {
        requireNotNull(invoiceNo, "invoiceNo");
        requireNotNull(transactionId, "transactionId");
        requireNotNull(subTotal, "subTotal");
        requireNotNull(taxAmount, "taxAmount");
        requireNotNull(totalAmount, "totalAmount");

        transactionRepository.findById(transactionId)
                .orElseThrow(() -> new DomainException("Transaction not found: " + transactionId));

        Invoice invoice = new Invoice(invoiceNo, transactionId, subTotal, taxAmount, totalAmount);
        return invoiceRepository.save(invoice);
    }

    /**
     * Cancels an issued invoice.
     *
     * @param invoiceId the UUID of the invoice to cancel
     * @return the updated {@link Invoice}
     * @throws DomainException if the invoice is not found
     */
    @Override
    public Invoice cancelInvoice(UUID invoiceId) {
        requireNotNull(invoiceId, "invoiceId");

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DomainException("Invoice not found: " + invoiceId));

        invoice.cancel();
        return invoiceRepository.save(invoice);
    }

    /**
     * Registers a new payment method.
     *
     * @param cardType   the card network type
     * @param lastFour   the last four digits of the card
     * @param expiryDate the card expiry date
     * @return the persisted {@link PaymentMethod}
     */
    @Override
    public PaymentMethod registerPaymentMethod(CardType cardType, String lastFour, LocalDate expiryDate) {
        requireNotNull(cardType, "cardType");
        requireNotNull(lastFour, "lastFour");
        requireNotNull(expiryDate, "expiryDate");

        PaymentMethod paymentMethod = new PaymentMethod(cardType, lastFour, expiryDate);
        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * Looks up the correct {@link PaymentProcessor} by card type (null = QR code),
     * creates a {@link PaymentIntent}, and verifies it.
     *
     * <p>If {@link PaymentProcessor#verify} returns {@code false}, a {@link DomainException}
     * is thrown.
     *
     * @param transaction the transaction to process
     * @param cardType    the card type of the payment method, or {@code null} for QR code
     * @param gatewayRef  the gateway reference to verify
     * @return the created and verified {@link PaymentIntent}
     * @throws DomainException if no processor is found or verification fails
     */
    public PaymentIntent processPayment(Transaction transaction, CardType cardType, String gatewayRef) {
        requireNotNull(transaction, "transaction");
        requireNotNull(gatewayRef, "gatewayRef");

        PaymentProcessor processor = processors.stream()
                .filter(p -> Objects.equals(p.getSupportedCardType(), cardType))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "No PaymentProcessor found for card type: " + cardType));

        PaymentIntent intent = processor.createIntent(transaction);

        if (!processor.verify(gatewayRef, transaction.getAmount())) {
            throw new DomainException("Payment verification failed for gatewayRef: " + gatewayRef);
        }

        return intent;
    }

    /**
     * Generates the daily platform revenue snapshot for the given date.
     *
     * <p>If a snapshot already exists for the date, it is returned as-is.
     * Otherwise, a new snapshot is created and persisted.
     *
     * @param snapshotDate the date to generate the snapshot for
     * @return the persisted {@link PlatformRevenueSnapshot}
     */
    @Override
    public PlatformRevenueSnapshot generateDailySnapshot(LocalDate snapshotDate) {
        requireNotNull(snapshotDate, "snapshotDate");

        return platformRevenueSnapshotRepository.findByDate(snapshotDate)
                .orElseGet(() -> {
                    PlatformRevenueSnapshot snapshot = new PlatformRevenueSnapshot(snapshotDate, 0);
                    return platformRevenueSnapshotRepository.save(snapshot);
                });
    }
}

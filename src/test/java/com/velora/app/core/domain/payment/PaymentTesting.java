package com.velora.app.modules.paymentModule.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.UUID;

import org.junit.Test;

import com.velora.app.common.DomainException;
import com.velora.app.modules.paymentModule.domain.CardType;
import com.velora.app.modules.paymentModule.domain.Invoice;
import com.velora.app.modules.paymentModule.domain.InvoiceStatus;
import com.velora.app.modules.paymentModule.domain.PayerType;
import com.velora.app.modules.paymentModule.domain.PaymentIntent;
import com.velora.app.modules.paymentModule.domain.PaymentIntentStatus;
import com.velora.app.modules.paymentModule.domain.PaymentMethod;
import com.velora.app.modules.paymentModule.domain.PlatformRevenueSnapshot;
import com.velora.app.modules.paymentModule.domain.Transaction;
import com.velora.app.modules.paymentModule.domain.TransactionStatus;

public class PaymentTesting {

    @Test
    public void paymentMethod_validCreationAndOptionalToken() {
        PaymentMethod method = new PaymentMethod(CardType.VISA, "1234", LocalDate.now().plusMonths(2));
        assertNotNull(method.getMethodId());

        method.setGatewayToken("enc_token");
        assertEquals("enc_token", method.getGatewayToken());
    }

    @Test(expected = IllegalArgumentException.class)
    public void paymentMethod_invalidLastFour_throws() {
        new PaymentMethod(CardType.AMEX, "12A4", LocalDate.now().plusMonths(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void paymentMethod_pastExpiry_throws() {
        new PaymentMethod(CardType.MASTERCARD, "9999", LocalDate.now().minusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void paymentMethod_blankGatewayToken_throws() {
        PaymentMethod method = new PaymentMethod(CardType.VISA, "1234", LocalDate.now().plusMonths(2));
        method.setGatewayToken("   ");
    }

    @Test
    public void paymentIntent_markSuccess_setsStatus() {
        PaymentIntent intent = new PaymentIntent(UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.now().plusMinutes(10));
        assertEquals(PaymentIntentStatus.PENDING, intent.getStatus());

        intent.setQrCodeData("qr");
        intent.markSuccess();
        assertEquals(PaymentIntentStatus.SUCCESS, intent.getStatus());
    }

    @Test(expected = DomainException.class)
    public void paymentIntent_illegalTransitionFromSuccess_throws() {
        PaymentIntent intent = new PaymentIntent(UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.now().plusMinutes(10));
        intent.markSuccess();
        intent.markFailed();
    }

    @Test(expected = IllegalArgumentException.class)
    public void paymentIntent_expiresAtNotFuture_throws() {
        new PaymentIntent(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now().minusSeconds(1));
    }

    @Test
    public void transaction_amountNormalizedAndPaidAtSet() {
        Transaction txn = new Transaction(new BigDecimal("10"), Currency.getInstance("USD"), PayerType.USER,
                UUID.randomUUID());
        assertEquals(new BigDecimal("10.00"), txn.getAmount());
        assertEquals(TransactionStatus.PENDING, txn.getStatus());

        txn.markPaid();
        assertEquals(TransactionStatus.PAID, txn.getStatus());
        assertNotNull(txn.getPaidAt());
        assertNotNull(txn.getCreatedAt());
    }

    @Test(expected = DomainException.class)
    public void transaction_markPaidTwice_throws() {
        Transaction txn = new Transaction(new BigDecimal("10"), Currency.getInstance("USD"), PayerType.SHOP,
                UUID.randomUUID());
        txn.markPaid();
        txn.markPaid();
    }

    @Test(expected = DomainException.class)
    public void transaction_markFailedAfterPaid_throws() {
        Transaction txn = new Transaction(new BigDecimal("10"), Currency.getInstance("USD"), PayerType.SHOP,
                UUID.randomUUID());
        txn.markPaid();
        txn.markFailed();
    }

    @Test(expected = DomainException.class)
    public void transaction_gatewayRefCannotChangeOnceSet_throws() {
        Transaction txn = new Transaction(new BigDecimal("10"), Currency.getInstance("USD"), PayerType.USER,
                UUID.randomUUID());
        txn.setGatewayRef("gw_1");
        txn.setGatewayRef("gw_2");
    }

    @Test
    public void invoice_verifyTotalAndCancel() {
        Invoice invoice = new Invoice("INV-001", UUID.randomUUID(), new BigDecimal("100"), new BigDecimal("10"),
                new BigDecimal("110"));
        invoice.verifyTotal();
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());

        invoice.cancel();
        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
    }

    @Test(expected = DomainException.class)
    public void invoice_verifyTotalMismatch_throws() {
        Invoice invoice = new Invoice("INV-002", UUID.randomUUID(), new BigDecimal("100"), new BigDecimal("10"),
                new BigDecimal("110"));
        invoice.setDiscountPrice(new BigDecimal("5"));
        invoice.verifyTotal();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invoice_negativeMoney_throws() {
        new Invoice("INV-003", UUID.randomUUID(), new BigDecimal("-1"), new BigDecimal("0"), new BigDecimal("0"));
    }

    @Test
    public void platformRevenueSnapshot_aggregatesInvoices() {
        Invoice i1 = new Invoice("INV-010", UUID.randomUUID(), new BigDecimal("50"), new BigDecimal("5"),
                new BigDecimal("55"));
        Invoice i2 = new Invoice("INV-011", UUID.randomUUID(), new BigDecimal("10"), new BigDecimal("1"),
                new BigDecimal("11"));

        PlatformRevenueSnapshot snapshot = PlatformRevenueSnapshot.fromInvoices(LocalDate.now(), 3,
                Arrays.asList(i1, i2));
        assertEquals(new BigDecimal("66.00"), snapshot.getTotalRevenue());
        assertEquals(i2.getInvoiceId(), snapshot.getLastInvoiceId());
        assertNotNull(snapshot.getPlatformSnapId());
    }
}


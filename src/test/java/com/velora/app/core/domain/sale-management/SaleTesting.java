package com.velora.app.core.domain.salemanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

public class SaleTesting {

    @Test
    public void orderItem_subtotalCalculatedAndMoneyNormalized() {
        UUID orderId = UUID.randomUUID();
        OrderItem item = new OrderItem(orderId, UUID.randomUUID(), 2, new BigDecimal("1"));
        assertEquals(new BigDecimal("2.00"), item.getSubtotal());
        assertEquals(new BigDecimal("1.00"), item.getSoldPrice());
        assertEquals(orderId, item.getOrderId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void orderItem_invalidQuantity_throws() {
        new OrderItem(UUID.randomUUID(), UUID.randomUUID(), 0, new BigDecimal("1.00"));
    }

    @Test
    public void order_verifyTotalAndTransitions() {
        UUID orderId = UUID.randomUUID();
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(orderId, UUID.randomUUID(), 1, new BigDecimal("3.50")));
        items.add(new OrderItem(orderId, UUID.randomUUID(), 2, new BigDecimal("1.25")));

        Order order = new Order(orderId, UUID.randomUUID(), UUID.randomUUID(), items);
        order.verifyTotal();
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("6.00"), order.getTotalPrice());

        order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertTrue(order.isFinalized());

        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.isFinalized());
    }

    @Test(expected = IllegalStateException.class)
    public void order_markPaidTwice_throws() {
        UUID orderId = UUID.randomUUID();
        List<OrderItem> items = List.of(new OrderItem(orderId, UUID.randomUUID(), 1, new BigDecimal("1.00")));
        Order order = new Order(orderId, UUID.randomUUID(), UUID.randomUUID(), items);
        order.markPaid();
        order.markPaid();
    }

    @Test
    public void receipt_generateNumberAndConfirmIdempotent() {
        Receipt receipt = new Receipt(UUID.randomUUID());
        assertNotNull(receipt.getReceiptId());
        assertNotNull(receipt.getIssuedAt());
        assertTrue(receipt.getReceiptNumber().matches("^INV-\\d{4}$"));

        receipt.confirmPayment("bank_123");
        assertTrue(receipt.isPaid());
        assertEquals("bank_123", receipt.getBankTransactionRef());

        // idempotent with same reference
        receipt.confirmPayment("bank_123");
        assertEquals("bank_123", receipt.getBankTransactionRef());
    }

    @Test(expected = IllegalStateException.class)
    public void receipt_confirmPaymentDifferentRef_throws() {
        Receipt receipt = new Receipt(UUID.randomUUID());
        receipt.confirmPayment("bank_1");
        receipt.confirmPayment("bank_2");
    }

    @Test
    public void delivery_stateMachineHappyPath() {
        Delivery delivery = new Delivery(UUID.randomUUID(), "Main street");
        assertEquals(DeliveryStatus.PENDING, delivery.getStatus());

        delivery.dispatch();
        assertEquals(DeliveryStatus.IN_TRANSIT, delivery.getStatus());

        delivery.complete();
        assertEquals(DeliveryStatus.DELIVERED, delivery.getStatus());
        assertNotNull(delivery.getCompletedAt());
    }

    @Test(expected = IllegalStateException.class)
    public void delivery_cannotDispatchAfterFailed() {
        Delivery delivery = new Delivery(UUID.randomUUID(), "Main street");
        delivery.fail("no courier");
        delivery.dispatch();
    }

    @Test
    public void paymentIntent_confirmAndExpire() {
        List<PaymentIntent.CartItemSnapshot> items = List.of(
                new PaymentIntent.CartItemSnapshot(UUID.randomUUID(), 1, new BigDecimal("2.50")));
        PaymentIntent intent = new PaymentIntent("bankRef", UUID.randomUUID(), UUID.randomUUID(), items, "{cart}");
        assertEquals(PaymentIntentStatus.CREATED, intent.getStatus());
        assertTrue(intent.isValid());

        intent.confirm();
        assertEquals(PaymentIntentStatus.CONFIRMED, intent.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void paymentIntent_confirmAfterExpire_throws() {
        List<PaymentIntent.CartItemSnapshot> items = List.of(
                new PaymentIntent.CartItemSnapshot(UUID.randomUUID(), 1, new BigDecimal("2.50")));
        PaymentIntent intent = new PaymentIntent("bankRef", UUID.randomUUID(), UUID.randomUUID(), items, "{cart}");
        intent.expire();
        intent.confirm();
    }

    @Test
    public void orderService_endToEnd_finalizeAtomicWithDelivery() {
        InMemoryPaymentIntentStore intentStore = new InMemoryPaymentIntentStore();
        InMemoryOrderStore orderStore = new InMemoryOrderStore();
        InMemoryReceiptStore receiptStore = new InMemoryReceiptStore();
        InMemoryDeliveryStore deliveryStore = new InMemoryDeliveryStore();

        RecordingInventoryService inventoryService = new RecordingInventoryService();
        InMemoryWebhookSecurity webhookSecurity = new InMemoryWebhookSecurity();
        TransactionRunner tx = work -> work.run();

        OrderService service = new OrderService(tx, intentStore, orderStore, receiptStore, deliveryStore,
                inventoryService, webhookSecurity);

        UUID shopId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        List<PaymentIntent.CartItemSnapshot> snapshotItems = List.of(
                new PaymentIntent.CartItemSnapshot(p1, 2, new BigDecimal("1.00")),
                new PaymentIntent.CartItemSnapshot(p2, 1, new BigDecimal("3.50")));

        PaymentIntent intent = service.createIntent("bankRef_1", shopId, customerId, snapshotItems, "{cart}");
        assertEquals(PaymentIntentStatus.CREATED, intent.getStatus());
        assertEquals(new BigDecimal("5.50"), intent.getTotalAmount());

        OrderService.PaymentWebhookEvent event = new OrderService.PaymentWebhookEvent("bankRef_1",
                new BigDecimal("5.50"), "payload", "sig", "nonce_1");
        PaymentIntent confirmed = service.verifyPayment(event);
        assertEquals(PaymentIntentStatus.CONFIRMED, confirmed.getStatus());

        Order order = service.finalizeOrderAtomic(intent.getIntentId(), true, "Addr");
        assertNotNull(order);
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(new BigDecimal("5.50"), order.getTotalPrice());

        assertTrue(intentStore.findByBankRefId("bankRef_1").isEmpty());
        assertNotNull(orderStore.findById(order.getOrderId()).orElseThrow());
        assertNotNull(receiptStore.findByOrderId(order.getOrderId()).orElseThrow());
        assertNotNull(deliveryStore.findByOrderId(order.getOrderId()).orElseThrow());

        Map<UUID, Integer> deducted = inventoryService.lastHardDeduct;
        assertEquals(Integer.valueOf(2), deducted.get(p1));
        assertEquals(Integer.valueOf(1), deducted.get(p2));
    }

    @Test(expected = IllegalStateException.class)
    public void orderService_rejectsMismatchedAmount() {
        InMemoryPaymentIntentStore intentStore = new InMemoryPaymentIntentStore();
        OrderService service = new OrderService(work -> work.run(), intentStore, new InMemoryOrderStore(),
                new InMemoryReceiptStore(), new InMemoryDeliveryStore(), new RecordingInventoryService(),
                new InMemoryWebhookSecurity());

        List<PaymentIntent.CartItemSnapshot> snapshotItems = List.of(
                new PaymentIntent.CartItemSnapshot(UUID.randomUUID(), 1, new BigDecimal("2.00")));
        service.createIntent("bankRef_m", UUID.randomUUID(), UUID.randomUUID(), snapshotItems, "{cart}");

        service.verifyPayment(new OrderService.PaymentWebhookEvent("bankRef_m", new BigDecimal("9.99"), "p", "s",
                "n"));
    }

    @Test(expected = IllegalStateException.class)
    public void orderService_rejectsDuplicateBankRefId() {
        InMemoryPaymentIntentStore intentStore = new InMemoryPaymentIntentStore();
        OrderService service = new OrderService(work -> work.run(), intentStore, new InMemoryOrderStore(),
                new InMemoryReceiptStore(), new InMemoryDeliveryStore(), new RecordingInventoryService(),
                new InMemoryWebhookSecurity());

        List<PaymentIntent.CartItemSnapshot> snapshotItems = List.of(
                new PaymentIntent.CartItemSnapshot(UUID.randomUUID(), 1, new BigDecimal("2.00")));
        service.createIntent("bankRef_dup", UUID.randomUUID(), UUID.randomUUID(), snapshotItems, "{cart}");
        service.createIntent("bankRef_dup", UUID.randomUUID(), UUID.randomUUID(), snapshotItems, "{cart}");
    }

    private static class InMemoryPaymentIntentStore implements PaymentIntentStore {
        private final Map<UUID, PaymentIntent> byId = new HashMap<>();
        private final Map<String, UUID> byBankRef = new HashMap<>();

        @Override
        public boolean existsByBankRefId(String bankRefId) {
            return byBankRef.containsKey(bankRefId);
        }

        @Override
        public PaymentIntent save(PaymentIntent intent) {
            byId.put(intent.getIntentId(), intent);
            byBankRef.put(intent.getBankRefId(), intent.getIntentId());
            return intent;
        }

        @Override
        public Optional<PaymentIntent> getForUpdate(UUID intentId) {
            return Optional.ofNullable(byId.get(intentId));
        }

        @Override
        public Optional<PaymentIntent> findByBankRefId(String bankRefId) {
            UUID id = byBankRef.get(bankRefId);
            if (id == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public void delete(UUID intentId) {
            PaymentIntent removed = byId.remove(intentId);
            if (removed != null) {
                byBankRef.remove(removed.getBankRefId());
            }
        }
    }

    private static class InMemoryOrderStore implements OrderStore {
        private final Map<UUID, Order> byId = new HashMap<>();

        @Override
        public Order save(Order order) {
            byId.put(order.getOrderId(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(UUID orderId) {
            return Optional.ofNullable(byId.get(orderId));
        }
    }

    private static class InMemoryReceiptStore implements ReceiptStore {
        private final Map<UUID, Receipt> byOrderId = new HashMap<>();

        @Override
        public Receipt save(Receipt receipt) {
            byOrderId.put(receipt.getOrderId(), receipt);
            return receipt;
        }

        @Override
        public Optional<Receipt> findByOrderId(UUID orderId) {
            return Optional.ofNullable(byOrderId.get(orderId));
        }
    }

    private static class InMemoryDeliveryStore implements DeliveryStore {
        private final Map<UUID, Delivery> byOrderId = new HashMap<>();

        @Override
        public Delivery save(Delivery delivery) {
            byOrderId.put(delivery.getOrderId(), delivery);
            return delivery;
        }

        @Override
        public Optional<Delivery> findByOrderId(UUID orderId) {
            return Optional.ofNullable(byOrderId.get(orderId));
        }
    }

    private static class RecordingInventoryService implements InventoryService {
        Map<UUID, Integer> lastSoftCheck;
        Map<UUID, Integer> lastHardDeduct;

        @Override
        public void softCheckStock(UUID shopId, Map<UUID, Integer> productQuantities) {
            lastSoftCheck = new HashMap<>(productQuantities);
        }

        @Override
        public void hardDeductStock(UUID shopId, Map<UUID, Integer> productQuantities) {
            lastHardDeduct = new HashMap<>(productQuantities);
        }
    }

    private static class InMemoryWebhookSecurity implements WebhookSecurity {
        private final Map<String, Boolean> seenNonces = new HashMap<>();

        @Override
        public void verifySignature(String payload, String signature) {
            // noop for test
        }

        @Override
        public void verifyNotReplayed(String nonce) {
            if (seenNonces.putIfAbsent(nonce, Boolean.TRUE) != null) {
                throw new IllegalStateException("replayed nonce");
            }
        }
    }
}

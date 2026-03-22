package com.velora.app.core.domain.salemanagement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Domain orchestrator for payment intents and atomic order finalization.
 */
public class OrderService {

    public record PaymentWebhookEvent(String bankRefId, BigDecimal paidAmount, String payload, String signature,
            String nonce) {
        public PaymentWebhookEvent {
            ValidationUtils.validateNotBlank(bankRefId, "bankRefId");
            paidAmount = ValidationUtils.normalizeMoney(paidAmount, "paidAmount");
            ValidationUtils.validateNotBlank(payload, "payload");
            ValidationUtils.validateNotBlank(signature, "signature");
            ValidationUtils.validateNotBlank(nonce, "nonce");
        }
    }

    private final TransactionRunner transactionRunner;
    private final PaymentIntentStore intentStore;
    private final OrderStore orderStore;
    private final ReceiptStore receiptStore;
    private final DeliveryStore deliveryStore;
    private final InventoryService inventoryService;
    private final WebhookSecurity webhookSecurity;

    public OrderService(TransactionRunner transactionRunner, PaymentIntentStore intentStore, OrderStore orderStore,
            ReceiptStore receiptStore, DeliveryStore deliveryStore, InventoryService inventoryService,
            WebhookSecurity webhookSecurity) {
        this.transactionRunner = require(transactionRunner, "transactionRunner");
        this.intentStore = require(intentStore, "intentStore");
        this.orderStore = require(orderStore, "orderStore");
        this.receiptStore = require(receiptStore, "receiptStore");
        this.deliveryStore = require(deliveryStore, "deliveryStore");
        this.inventoryService = require(inventoryService, "inventoryService");
        this.webhookSecurity = require(webhookSecurity, "webhookSecurity");
    }

    /**
     * Creates a new payment intent from an immutable snapshot.
     */
    public PaymentIntent createIntent(String bankRefId, UUID shopId, UUID customerId,
            List<PaymentIntent.CartItemSnapshot> snapshotItems, String cartSnapshot) {
        ValidationUtils.validateNotBlank(bankRefId, "bankRefId");
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateUUID(customerId, "customerId");
        ValidationUtils.validateNotBlank(snapshotItems, "snapshotItems");
        if (intentStore.existsByBankRefId(bankRefId)) {
            throw new IllegalStateException("Duplicate bankRefId rejected");
        }
        PaymentIntent intent = new PaymentIntent(bankRefId, shopId, customerId, snapshotItems, cartSnapshot);
        inventoryService.softCheckStock(shopId, toProductQuantities(snapshotItems));
        return intentStore.save(intent);
    }

    /**
     * Verifies an incoming webhook and confirms the corresponding intent.
     * <p>
     * Idempotent: re-confirming an already confirmed intent is allowed.
     */
    public PaymentIntent verifyPayment(PaymentWebhookEvent event) {
        webhookSecurity.verifySignature(event.payload(), event.signature());
        webhookSecurity.verifyNotReplayed(event.nonce());

        PaymentIntent intent = intentStore.findByBankRefId(event.bankRefId())
                .orElseThrow(() -> new IllegalStateException("PaymentIntent not found for bankRefId"));

        if (intent.getTotalAmount().compareTo(event.paidAmount()) != 0) {
            throw new IllegalStateException("Mismatched amount rejected");
        }
        if (intent.getStatus() == PaymentIntentStatus.CONFIRMED) {
            return intent;
        }
        if (intent.getStatus() != PaymentIntentStatus.CREATED) {
            throw new IllegalStateException("Intent not confirmable in status " + intent.getStatus());
        }
        intent.confirm();
        return intentStore.save(intent);
    }

    /**
     * Finalizes an order atomically inside one transaction.
     */
    public Order finalizeOrderAtomic(UUID intentId, boolean deliveryNeeded, String deliveryAddress) {
        ValidationUtils.validateUUID(intentId, "intentId");
        if (deliveryNeeded) {
            ValidationUtils.validateNotBlank(deliveryAddress, "deliveryAddress");
        }

        final Order[] resultHolder = new Order[1];

        transactionRunner.runInTransaction(() -> {
            PaymentIntent intent = intentStore.getForUpdate(intentId)
                    .orElseThrow(() -> new IllegalStateException("PaymentIntent not found"));

            if (intent.getStatus() != PaymentIntentStatus.CONFIRMED) {
                throw new IllegalStateException("PaymentIntent must be CONFIRMED to finalize");
            }

            UUID orderId = UUID.randomUUID();
            List<OrderItem> items = new ArrayList<>();
            for (PaymentIntent.CartItemSnapshot snap : intent.getSnapshotItems()) {
                items.add(new OrderItem(orderId, snap.productId(), snap.quantity(), snap.soldPrice()));
            }

            Order order = new Order(orderId, intent.getShopId(), intent.getCustomerId(), items);

            inventoryService.hardDeductStock(intent.getShopId(), toProductQuantities(intent.getSnapshotItems()));

            Receipt receipt = new Receipt(order.getId());
            receipt.confirmPayment(intent.getBankRefId());

            order.markPaid();

            orderStore.save(order);
            receiptStore.save(receipt);

            if (deliveryNeeded) {
                Delivery delivery = new Delivery(order.getId(), deliveryAddress);
                deliveryStore.save(delivery);
            }

            intentStore.delete(intent.getId());
            resultHolder[0] = order;
        });

        return resultHolder[0];
    }

    /**
     * Deletes a payment intent (post-transaction cleanup).
     */
    public void cleanupIntent(UUID intentId) {
        ValidationUtils.validateUUID(intentId, "intentId");
        intentStore.delete(intentId);
    }

    /**
     * Expires intents that are no longer valid.
     * <p>
     * Callers provide candidate IDs (selection is an application concern).
     *
     * @return number of intents expired
     */
    public int expireOldIntents(List<UUID> intentIds) {
        ValidationUtils.validateNotBlank(intentIds, "intentIds");
        final int[] expiredCount = new int[] { 0 };

        for (UUID intentId : intentIds) {
            ValidationUtils.validateUUID(intentId, "intentId");
            transactionRunner.runInTransaction(() -> {
                PaymentIntent intent = intentStore.getForUpdate(intentId).orElse(null);
                if (intent == null) {
                    return;
                }
                if (intent.getStatus() == PaymentIntentStatus.CREATED && !intent.isValid()) {
                    intent.expire();
                    intentStore.save(intent);
                    expiredCount[0]++;
                }
            });
        }

        return expiredCount[0];
    }

    /**
     * Cancels pending orders.
     * <p>
     * Callers provide candidate IDs (selection is an application concern).
     *
     * @return number of orders cancelled
     */
    public int cancelUnpaidOrders(List<UUID> orderIds) {
        ValidationUtils.validateNotBlank(orderIds, "orderIds");
        int cancelled = 0;

        for (UUID orderId : orderIds) {
            ValidationUtils.validateUUID(orderId, "orderId");
            Order order = orderStore.findById(orderId).orElse(null);
            if (order == null) {
                continue;
            }
            if (order.getStatus() == OrderStatus.PENDING) {
                order.cancel();
                orderStore.save(order);
                cancelled++;
            }
        }

        return cancelled;
    }

    /**
     * Marks deliveries as failed.
     * <p>
     * Callers provide candidate order IDs.
     *
     * @return number of deliveries failed
     */
    public int markFailedDeliveries(List<UUID> orderIds, String reason) {
        ValidationUtils.validateNotBlank(orderIds, "orderIds");
        ValidationUtils.validateNotBlank(reason, "reason");

        int failed = 0;
        for (UUID orderId : orderIds) {
            ValidationUtils.validateUUID(orderId, "orderId");
            Delivery delivery = deliveryStore.findByOrderId(orderId).orElse(null);
            if (delivery == null) {
                continue;
            }
            if (delivery.getStatus() == DeliveryStatus.DELIVERED || delivery.getStatus() == DeliveryStatus.FAILED) {
                continue;
            }
            delivery.fail(reason);
            deliveryStore.save(delivery);
            failed++;
        }
        return failed;
    }

    /**
     * Replays payment verification for a set of webhook events.
     * <p>
     * verifyPayment is idempotent.
     *
     * @return number of events processed
     */
    public int reconcilePayments(List<PaymentWebhookEvent> events) {
        ValidationUtils.validateNotBlank(events, "events");
        int processed = 0;
        for (PaymentWebhookEvent event : events) {
            ValidationUtils.validateNotBlank(event, "event");
            verifyPayment(event);
            processed++;
        }
        return processed;
    }

    private static Map<UUID, Integer> toProductQuantities(List<PaymentIntent.CartItemSnapshot> snapshotItems) {
        Map<UUID, Integer> quantities = new HashMap<>();
        for (PaymentIntent.CartItemSnapshot item : snapshotItems) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantities;
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}

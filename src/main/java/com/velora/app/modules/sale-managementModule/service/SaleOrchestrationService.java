package com.velora.app.modules.sale_managementModule.service;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.modules.sale_managementModule.domain.DeliveryStore;
import com.velora.app.modules.sale_managementModule.domain.InventoryService;
import com.velora.app.modules.sale_managementModule.domain.Order;
import com.velora.app.modules.sale_managementModule.domain.OrderService;
import com.velora.app.modules.sale_managementModule.domain.OrderStore;
import com.velora.app.modules.sale_managementModule.domain.PaymentIntent;
import com.velora.app.modules.sale_managementModule.domain.PaymentIntentStore;
import com.velora.app.modules.sale_managementModule.domain.ReceiptStore;
import com.velora.app.modules.sale_managementModule.domain.TransactionRunner;
import com.velora.app.modules.sale_managementModule.domain.WebhookSecurity;

import java.util.List;
import java.util.UUID;

/**
 * Application-layer service for sale orchestration: payment intents,
 * webhook handling, order finalization, and cleanup jobs.
 *
 * <p>
 * Extends {@link AbstractDomainService} to reuse {@code requireNotNull}
 * guard methods. Delegates domain logic to {@link OrderService}.
 *
 * <p>
 * Requirements: 16.6
 */
public class SaleOrchestrationService extends AbstractDomainService implements ISaleOrchestrationService {

    private final OrderService orderService;

    public SaleOrchestrationService(
            TransactionRunner transactionRunner,
            PaymentIntentStore paymentIntentStore,
            OrderStore orderStore,
            ReceiptStore receiptStore,
            DeliveryStore deliveryStore,
            InventoryService inventoryService,
            WebhookSecurity webhookSecurity) {
        this.orderService = new OrderService(
                transactionRunner,
                paymentIntentStore,
                orderStore,
                receiptStore,
                deliveryStore,
                inventoryService,
                webhookSecurity);
    }

    /**
     * Creates a new payment intent from a cart snapshot.
     *
     * @param bankRefId     the bank reference ID for the payment
     * @param shopId        the UUID of the shop
     * @param customerId    the UUID of the customer
     * @param snapshotItems the immutable cart item snapshots
     * @param cartSnapshot  the serialized cart snapshot string
     * @return the persisted {@link PaymentIntent}
     */
    @Override
    public PaymentIntent createPaymentIntent(String bankRefId, UUID shopId, UUID customerId,
            List<PaymentIntent.CartItemSnapshot> snapshotItems, String cartSnapshot) {
        requireNotNull(bankRefId, "bankRefId");
        requireNotNull(shopId, "shopId");
        requireNotNull(customerId, "customerId");
        requireNotNull(snapshotItems, "snapshotItems");

        return orderService.createIntent(bankRefId, shopId, customerId, snapshotItems, cartSnapshot);
    }

    /**
     * Verifies an incoming payment webhook and confirms the corresponding intent.
     *
     * @param event the webhook event containing bank reference, amount, and
     *              security fields
     * @return the confirmed {@link PaymentIntent}
     */
    @Override
    public PaymentIntent handlePaymentWebhook(OrderService.PaymentWebhookEvent event) {
        requireNotNull(event, "event");

        return orderService.verifyPayment(event);
    }

    /**
     * Atomically finalizes an order after payment confirmation.
     *
     * @param intentId        the UUID of the confirmed payment intent
     * @param deliveryNeeded  whether a delivery record should be created
     * @param deliveryAddress the delivery address (required when deliveryNeeded is
     *                        true)
     * @return the finalized {@link Order}
     */
    @Override
    public Order finalizeOrder(UUID intentId, boolean deliveryNeeded, String deliveryAddress) {
        requireNotNull(intentId, "intentId");

        return orderService.finalizeOrderAtomic(intentId, deliveryNeeded, deliveryAddress);
    }

    /**
     * Expires stale payment intents from the provided candidate list.
     *
     * @param intentIds the list of candidate intent UUIDs to evaluate
     * @return number of intents expired
     */
    @Override
    public int expireStaleIntents(List<UUID> intentIds) {
        requireNotNull(intentIds, "intentIds");

        return orderService.expireOldIntents(intentIds);
    }

    /**
     * Cancels pending orders from the provided candidate list.
     *
     * @param orderIds the list of candidate order UUIDs to evaluate
     * @return number of orders cancelled
     */
    @Override
    public int cancelUnpaidOrders(List<UUID> orderIds) {
        requireNotNull(orderIds, "orderIds");

        return orderService.cancelUnpaidOrders(orderIds);
    }
}

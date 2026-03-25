package com.velora.app.core.service;

import com.velora.app.core.domain.salemanagement.Order;
import com.velora.app.core.domain.salemanagement.OrderService;
import com.velora.app.core.domain.salemanagement.PaymentIntent;

import java.util.List;
import java.util.UUID;

/**
 * Application-layer contract for sale orchestration: payment intents, webhook handling,
 * order finalization, and cleanup jobs.
 *
 * <p>Requirement: 16.1, 16.6
 */
public interface ISaleOrchestrationService {

    /**
     * Creates a new payment intent from a cart snapshot.
     */
    PaymentIntent createPaymentIntent(String bankRefId, UUID shopId, UUID customerId,
            List<PaymentIntent.CartItemSnapshot> snapshotItems, String cartSnapshot);

    /**
     * Verifies an incoming payment webhook and confirms the corresponding intent.
     */
    PaymentIntent handlePaymentWebhook(OrderService.PaymentWebhookEvent event);

    /**
     * Atomically finalizes an order after payment confirmation.
     */
    Order finalizeOrder(UUID intentId, boolean deliveryNeeded, String deliveryAddress);

    /**
     * Expires stale payment intents from the provided candidate list.
     *
     * @return number of intents expired
     */
    int expireStaleIntents(List<UUID> intentIds);

    /**
     * Cancels pending orders from the provided candidate list.
     *
     * @return number of orders cancelled
     */
    int cancelUnpaidOrders(List<UUID> orderIds);
}

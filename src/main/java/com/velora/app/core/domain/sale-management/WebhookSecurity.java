package com.velora.app.core.domain.salemanagement;

/**
 * Security port for validating webhooks and preventing replay.
 */
public interface WebhookSecurity {

    /**
     * Validates the webhook signature.
     */
    void verifySignature(String payload, String signature);

    /**
     * Verifies that a nonce has not been used (replay protection).
     */
    void verifyNotReplayed(String nonce);
}

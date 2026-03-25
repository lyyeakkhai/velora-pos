package com.velora.app.core.domain.salemanagement;

import com.velora.app.common.DomainException;

/**
 * Security port for validating webhooks and preventing replay.
 */
public interface WebhookSecurity {

    /**
     * Validates the webhook signature.
     *
     * @throws DomainException if the signature is invalid
     */
    void verifySignature(String payload, String signature);

    /**
     * Verifies that a nonce has not been used (replay protection).
     *
     * @throws DomainException if the nonce has already been seen
     */
    void verifyNotReplayed(String nonce);
}

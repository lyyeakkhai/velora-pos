package com.velora.app.core.domain.notification;

/**
 * Outbound email gateway abstraction.
 */
public interface EmailGateway {

    /**
     * Sends an email to the given recipient.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body content
     */
    void send(String to, String subject, String body);
}

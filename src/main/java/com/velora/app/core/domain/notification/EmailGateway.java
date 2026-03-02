package com.velora.app.core.domain.notification;

import java.util.UUID;

/**
 * Outbound email gateway abstraction.
 */
public interface EmailGateway {

    void sendHighPriorityEmail(UUID userId, String title, String content, String linkUrl);
}

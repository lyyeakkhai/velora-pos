package com.velora.app.core.domain.notification;

/**
 * Delivery state for a notification in a specific channel.
 */
public enum DispatchStatus {
    PENDING,
    SENT,
    SKIPPED,
    FAILED
}

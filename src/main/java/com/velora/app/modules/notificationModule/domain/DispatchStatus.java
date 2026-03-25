package com.velora.app.modules.notificationModule.domain;

/**
 * Delivery state for a notification in a specific channel.
 */
public enum DispatchStatus {
    PENDING,
    SENT,
    SKIPPED,
    FAILED
}

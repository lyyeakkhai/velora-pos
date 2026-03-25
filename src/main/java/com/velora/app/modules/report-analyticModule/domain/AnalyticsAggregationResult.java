package com.velora.app.core.domain.reportandanalytic;

/**
 * Result for aggregation/job execution.
 */
public enum AnalyticsAggregationResult {
    COMPLETED,
    SKIPPED_ALREADY_EXISTS,
    SKIPPED_LOCKED
}

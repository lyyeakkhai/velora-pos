package com.velora.app.modules.report_analyticModule.domain;

/**
 * Result for aggregation/job execution.
 */
public enum AnalyticsAggregationResult {
    COMPLETED,
    SKIPPED_ALREADY_EXISTS,
    SKIPPED_LOCKED
}


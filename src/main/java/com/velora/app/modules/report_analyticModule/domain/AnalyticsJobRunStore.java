package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Persistence port tracking completed job runs for idempotency.
 */
public interface AnalyticsJobRunStore {

    boolean isCompleted(UUID shopId, LocalDate snapshotDate);

    void markCompleted(UUID shopId, LocalDate snapshotDate);
}


package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence port for audit/compliance execution logs.
 */
public interface AnalyticsJobLogStore {

    void log(UUID shopId, LocalDate snapshotDate, LogLevel level, String message, LocalDateTime atUtc);

    enum LogLevel {
        INFO,
        WARN,
        ERROR
    }
}


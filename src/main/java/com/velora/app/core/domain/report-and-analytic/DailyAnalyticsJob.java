package com.velora.app.core.domain.reportandanalytic;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Scheduled daily analytics aggregation job.
 * <p>
 * Intended to be triggered at 00:05 UTC.
 */
public class DailyAnalyticsJob {

    private final AnalyticsAggregationService aggregationService;
    private final Clock clock;

    public DailyAnalyticsJob(AnalyticsAggregationService aggregationService, Clock clock) {
        this.aggregationService = require(aggregationService, "aggregationService");
        this.clock = require(clock, "clock");
    }

    /**
     * Aggregates the UTC-yesterday window for the given shop.
     */
    public AnalyticsAggregationResult runForShop(UUID shopId, UUID orgId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateUUID(orgId, "orgId");
        LocalDate utcToday = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        LocalDate snapshotDate = utcToday.minusDays(1);
        return aggregationService.runDailyAggregation(shopId, orgId, snapshotDate, false);
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}

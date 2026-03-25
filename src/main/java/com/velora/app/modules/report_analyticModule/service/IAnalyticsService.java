package com.velora.app.modules.report_analyticModule.service;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.report_analyticModule.domain.AnalyticsAggregationResult;
import com.velora.app.modules.report_analyticModule.domain.CategoryTrendDTO;
import com.velora.app.modules.report_analyticModule.domain.DailyReportDTO;
import com.velora.app.modules.report_analyticModule.domain.OutOfStockPredictionDTO;
import com.velora.app.modules.report_analyticModule.domain.PeriodReportDTO;
import com.velora.app.modules.report_analyticModule.domain.SellerRankDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer contract for analytics aggregation and reporting.
 *
 * <p>
 * Requirement: 16.1, 16.10
 */
public interface IAnalyticsService {

        /**
         * Runs the full daily snapshot aggregation pipeline for a shop.
         */
        AnalyticsAggregationResult runDailyAggregation(UUID shopId, UUID orgId, LocalDate snapshotDate,
                        boolean allowVerifiedLossProfitNegative);

        /**
         * Returns the daily report for a shop on a given date.
         */
        DailyReportDTO getDailyReport(Role.RoleName actorRole, UUID shopId, LocalDate snapshotDate);

        /**
         * Returns the weekly period report ending on the given date.
         */
        PeriodReportDTO getWeeklyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive);

        /**
         * Returns the monthly period report ending on the given date.
         */
        PeriodReportDTO getMonthlyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive);

        /**
         * Returns the annual period report ending on the given date.
         */
        PeriodReportDTO getAnnualReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive);

        /**
         * Ranks sellers by gross revenue within the given date range.
         */
        List<SellerRankDTO> rankSellers(Role.RoleName actorRole, UUID shopId,
                        LocalDate startInclusive, LocalDate endInclusive);

        /**
         * Returns category revenue trends within the given date range.
         */
        List<CategoryTrendDTO> getCategoryTrends(Role.RoleName actorRole, UUID shopId,
                        LocalDate startInclusive, LocalDate endInclusive);

        /**
         * Predicts which product variants are at risk of going out of stock.
         */
        List<OutOfStockPredictionDTO> predictOutOfStock(Role.RoleName actorRole, UUID shopId,
                        LocalDate asOfDate, int horizonDays);
}

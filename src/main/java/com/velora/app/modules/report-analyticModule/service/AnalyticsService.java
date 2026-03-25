package com.velora.app.core.service.analytics;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.reportandanalytic.AnalyticsAggregationResult;
import com.velora.app.core.domain.reportandanalytic.AnalyticsAggregationService;
import com.velora.app.core.domain.reportandanalytic.CategoryAnalyticsService;
import com.velora.app.core.domain.reportandanalytic.CategoryTrendDTO;
import com.velora.app.core.domain.reportandanalytic.DailyReportDTO;
import com.velora.app.core.domain.reportandanalytic.ForecastService;
import com.velora.app.core.domain.reportandanalytic.OutOfStockPredictionDTO;
import com.velora.app.core.domain.reportandanalytic.PeriodReportDTO;
import com.velora.app.core.domain.reportandanalytic.ReportingService;
import com.velora.app.core.domain.reportandanalytic.SellerAnalyticsService;
import com.velora.app.core.domain.reportandanalytic.SellerRankDTO;
import com.velora.app.core.service.IAnalyticsService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer service for analytics aggregation and reporting.
 *
 * <p>Extends {@link AbstractDomainService} to reuse guard methods.
 * Delegates to domain services for aggregation, reporting, seller ranking,
 * category trends, and out-of-stock forecasting.
 *
 * <p>Requirements: 16.1, 16.10
 */
public class AnalyticsService extends AbstractDomainService implements IAnalyticsService {

    private final AnalyticsAggregationService aggregationService;
    private final ReportingService reportingService;
    private final SellerAnalyticsService sellerAnalyticsService;
    private final CategoryAnalyticsService categoryAnalyticsService;
    private final ForecastService forecastService;

    public AnalyticsService(
            AnalyticsAggregationService aggregationService,
            ReportingService reportingService,
            SellerAnalyticsService sellerAnalyticsService,
            CategoryAnalyticsService categoryAnalyticsService,
            ForecastService forecastService) {
        requireNotNull(aggregationService, "aggregationService");
        requireNotNull(reportingService, "reportingService");
        requireNotNull(sellerAnalyticsService, "sellerAnalyticsService");
        requireNotNull(categoryAnalyticsService, "categoryAnalyticsService");
        requireNotNull(forecastService, "forecastService");
        this.aggregationService = aggregationService;
        this.reportingService = reportingService;
        this.sellerAnalyticsService = sellerAnalyticsService;
        this.categoryAnalyticsService = categoryAnalyticsService;
        this.forecastService = forecastService;
    }

    /**
     * Runs the full daily snapshot aggregation pipeline for a shop.
     */
    @Override
    public AnalyticsAggregationResult runDailyAggregation(UUID shopId, UUID orgId, LocalDate snapshotDate,
            boolean allowVerifiedLossProfitNegative) {
        requireNotNull(shopId, "shopId");
        requireNotNull(orgId, "orgId");
        requireNotNull(snapshotDate, "snapshotDate");
        return aggregationService.runDailyAggregation(shopId, orgId, snapshotDate, allowVerifiedLossProfitNegative);
    }

    /**
     * Returns the daily report for a shop on a given date.
     */
    @Override
    public DailyReportDTO getDailyReport(Role.RoleName actorRole, UUID shopId, LocalDate snapshotDate) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(snapshotDate, "snapshotDate");
        return reportingService.getDailyReport(actorRole, shopId, snapshotDate);
    }

    /**
     * Returns the weekly period report ending on the given date.
     */
    @Override
    public PeriodReportDTO getWeeklyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(endDateInclusive, "endDateInclusive");
        return reportingService.getWeeklyReport(actorRole, shopId, endDateInclusive);
    }

    /**
     * Returns the monthly period report ending on the given date.
     */
    @Override
    public PeriodReportDTO getMonthlyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(endDateInclusive, "endDateInclusive");
        return reportingService.getMonthlyReport(actorRole, shopId, endDateInclusive);
    }

    /**
     * Returns the annual period report ending on the given date.
     */
    @Override
    public PeriodReportDTO getAnnualReport(Role.RoleName actorRole, UUID shopId, LocalDate endDateInclusive) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(endDateInclusive, "endDateInclusive");
        return reportingService.getAnnualReport(actorRole, shopId, endDateInclusive);
    }

    /**
     * Ranks sellers by gross revenue within the given date range.
     */
    @Override
    public List<SellerRankDTO> rankSellers(Role.RoleName actorRole, UUID shopId,
            LocalDate startInclusive, LocalDate endInclusive) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(startInclusive, "startInclusive");
        requireNotNull(endInclusive, "endInclusive");
        return sellerAnalyticsService.rankSellers(actorRole, shopId, startInclusive, endInclusive);
    }

    /**
     * Returns category revenue trends within the given date range.
     */
    @Override
    public List<CategoryTrendDTO> getCategoryTrends(Role.RoleName actorRole, UUID shopId,
            LocalDate startInclusive, LocalDate endInclusive) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(startInclusive, "startInclusive");
        requireNotNull(endInclusive, "endInclusive");
        return categoryAnalyticsService.getCategoryTrends(actorRole, shopId, startInclusive, endInclusive);
    }

    /**
     * Predicts which product variants are at risk of going out of stock.
     */
    @Override
    public List<OutOfStockPredictionDTO> predictOutOfStock(Role.RoleName actorRole, UUID shopId,
            LocalDate asOfDate, int horizonDays) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(asOfDate, "asOfDate");
        return forecastService.predictOutOfStock(actorRole, shopId, asOfDate, horizonDays);
    }
}

You are tasked with designing an enterprise-grade Reporting and Analytics Engine for a multi-tenant commerce platform (Velora / SkillHub).

This system is responsible for generating immutable, high-performance analytical data for financial reporting, seller performance, category trends, and AI-driven insights.

You must convert the following data warehouse schema and business logic into scalable Java services, aggregation jobs, and domain models.

--------------------------------------------------
DATA WAREHOUSE ENTITIES
--------------------------------------------------

1. DAILY_PRODUCT_SNAPSHOTS (Atomic Level)

   - snapshotId (UUID, PK)
   - snapshotDate (LocalDate, immutable)
   - productId (UUID, FK)
   - variantId (UUID, FK)
   - sellerId (UUID, FK)
   - categoryId (UUID, FK)
   - shopId (UUID, FK, RAS)
   - qtySold (int >= 0)
   - baseCostPrice (BigDecimal, scale=2, HALF_UP)
   - unitSalePrice (BigDecimal, scale=2, HALF_UP)
   - stockAtMidnight (int >= 0)
   - createdAt (LocalDateTime, immutable)

2. DAILY_CATEGORY_SNAPSHOTS (Middle Level)

   - snapshotId (UUID, PK)
   - snapshotDate (LocalDate)
   - categoryId (UUID, FK)
   - shopId (UUID, FK, RAS)
   - catGrossRevenue (BigDecimal)
   - catNetProfit (BigDecimal)
   - catItemsSold (int)
   - createdAt (LocalDateTime)

3. DAILY_SNAPSHOTS (High Level)

   - snapshotId (UUID, PK)
   - snapshotDate (LocalDate)
   - orgId (UUID, FK)
   - shopId (UUID, FK, RAS)
   - totalGross (BigDecimal)
   - totalProfit (BigDecimal)
   - orderCount (int)
   - createdAt (LocalDateTime)

--------------------------------------------------
CORE DESIGN PRINCIPLES
--------------------------------------------------

1. Immutability
   - All snapshots are write-once
   - No update or delete operations allowed
   - UUID and createdAt are immutable
   - Only INSERT operations permitted

2. Monetary Accuracy
   - All financial fields use BigDecimal
   - Scale = 2
   - RoundingMode.HALF_UP
   - No float or double allowed

3. Temporal Integrity
   - snapshotDate is normalized to UTC
   - One snapshot per shop per day
   - Duplicate daily snapshots prohibited

4. Row-Level Access Security (RAS)
   - All snapshot tables include shopId
   - All queries scoped by shopId
   - No cross-tenant aggregation

--------------------------------------------------
DATA AGGREGATION PIPELINE
--------------------------------------------------

Implement a scheduled analytics job (Cron / Batch Job):

DailyAnalyticsJob:

Runs once per day at 00:05 UTC

Steps:

1. Lock aggregation window (yesterday)
2. Fetch confirmed orders
3. Fetch order items
4. Fetch inventory history
5. Fetch seller assignments
6. Calculate aggregates
7. Persist snapshots
8. Mark job completed
9. Release lock

All steps must run inside a database transaction.

Idempotency:
   - Job must be re-runnable
   - If snapshot exists → skip

--------------------------------------------------
CALCULATION RULES
--------------------------------------------------

1. Product Snapshot

For each product + variant:

   qtySold = SUM(orderItem.quantity)

   unitSalePrice =
      AVG(orderItem.soldPrice)

   stockAtMidnight =
      inventorySnapshot.stock

2. Category Snapshot

   catGrossRevenue =
      SUM(qtySold * unitSalePrice)

   catNetProfit =
      SUM((unitSalePrice - baseCostPrice) * qtySold)

   catItemsSold =
      SUM(qtySold)

3. Daily Snapshot

   totalGross =
      SUM(catGrossRevenue)

   totalProfit =
      SUM(catNetProfit)

   orderCount =
      COUNT(distinct orderId)

--------------------------------------------------
BUSINESS SERVICES
--------------------------------------------------

AnalyticsAggregationService
   - runDailyAggregation()
   - validateSnapshotUniqueness()
   - persistSnapshots()

ReportingService
   - getDailyReport()
   - getWeeklyReport()
   - getMonthlyReport()
   - getAnnualReport()

SellerAnalyticsService
   - rankSellers()
   - getSellerPerformance()

CategoryAnalyticsService
   - getCategoryTrends()
   - comparePeriods()

ForecastService
   - predictOutOfStock()
   - detectRevenueDrop()

--------------------------------------------------
QUERY STRATEGY
--------------------------------------------------

Reports must use snapshots only.

No raw order scanning in dashboards.

Examples:

Weekly Report:
   SELECT SUM(totalGross)
   FROM daily_snapshots
   WHERE snapshotDate BETWEEN x AND y

Seller Ranking:
   GROUP BY sellerId
   ON daily_product_snapshots

Category Trend:
   Compare current vs previous period

--------------------------------------------------
AI INTEGRATION LAYER
--------------------------------------------------

Provide AI-ready DTOs:

AnalyticsInsightDTO
   - metricName
   - previousValue
   - currentValue
   - changePercent
   - riskLevel
   - recommendation

AI must consume snapshot tables only.

No direct transactional data access.

--------------------------------------------------
DATA QUALITY RULES
--------------------------------------------------

- No negative revenue
- No negative profit unless verified loss
- No duplicate rows
- No missing categories
- No orphan seller records
- No null financial fields

--------------------------------------------------
AUDIT & COMPLIANCE
--------------------------------------------------

- Full job execution logs
- Aggregation timestamps
- Error tracking
- Reconciliation reports
- Checksum validation

--------------------------------------------------
SECURITY
--------------------------------------------------

- Role validation on all report access
- OWNER: full analytics
- MANAGER: category/seller analytics
- SELLER: personal performance only
- Prevent data leakage

--------------------------------------------------
CODE QUALITY
--------------------------------------------------

- Clean architecture
- Domain-driven design
- JavaDoc on public APIs
- equals/hashCode via UUID
- No SQL in entities
- Repository abstraction
- Centralized validation
- No duplicated calculations

--------------------------------------------------
OUTPUT FORMAT
--------------------------------------------------

- Java source code only
- No markdown
- No explanations
- No pseudocode
- Production-ready

--------------------------------------------------
GOAL
--------------------------------------------------

Generate a scalable, immutable, high-performance analytics engine that supports:

- Real-time dashboards
- Financial reporting
- Seller rankings
- Category trends
- AI recommendations
- Forecasting
- Audit compliance

The result must represent enterprise data-warehouse-grade software, not simple reporting utilities.
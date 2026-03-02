You are tasked with designing an enterprise-grade Platform Revenue and Profit Tracking System for a multi-tenant SaaS commerce platform (Velora / SkillHub).

This system represents the financial “source of truth” for the platform owner and must support taxation, auditing, investor reporting, and long-term growth analysis.

You must convert the following schema and business rules into robust Java domain models, services, aggregation jobs, and reporting pipelines.

--------------------------------------------------
DATA ENTITY
--------------------------------------------------

1. PLATFORM_REVENUE_SNAPSHOTS

   - platformSnapId (UUID, PK, immutable)
   - snapshotDate (LocalDate, unique, required)
   - totalSubscriptionRevenue (BigDecimal, scale=2)
   - platformNetProfit (BigDecimal, scale=2)
   - activePayingShops (Integer)
   - createdAt (LocalDateTime, immutable)

--------------------------------------------------
CORE DESIGN PRINCIPLES
--------------------------------------------------

1. Financial Accuracy
   - All monetary values must use java.math.BigDecimal
   - Scale = 2
   - RoundingMode = HALF_UP
   - No double/float allowed

2. Immutability
   - platformSnapId and createdAt cannot be modified
   - Daily snapshots are append-only
   - No update/delete after finalization

3. Auditability
   - Every snapshot must be traceable
   - All calculations must be reproducible
   - Raw sources must be preserved

4. Security
   - Read-only for non-admin users
   - Only SYSTEM/ADMIN can generate snapshots
   - No manual edits

5. Performance
   - Indexed by snapshotDate
   - Cached summary queries
   - Pre-aggregated metrics only

--------------------------------------------------
DATA SOURCES
--------------------------------------------------

Platform revenue must be calculated from:

1. TRANSACTIONS
   - Subscription payments
   - Refund records

2. PLATFORM_REGISTRY
   - Active shop subscriptions
   - Plan types

3. DAILY_SNAPSHOTS
   - Shop-level revenue (for commissions)

4. INFRASTRUCTURE_COSTS
   - Server costs
   - API costs
   - Third-party fees

--------------------------------------------------
CALCULATION RULES
--------------------------------------------------

1. Total Subscription Revenue

   SUM(all successful subscription payments for the day)

2. Commission Revenue (Optional)

   IF commissionEnabled == true:
      commission = SUM(daily_snapshots.total_gross) * commissionRate

3. Platform Net Profit

   platformNetProfit =
      totalSubscriptionRevenue
    + commissionRevenue
    - infrastructureCosts

4. Active Paying Shops

   COUNT(DISTINCT shop_id)
   WHERE payment_status = PAID
   AND snapshotDate = today

--------------------------------------------------
SNAPSHOT GENERATION PIPELINE
--------------------------------------------------

RevenueAggregationJob (Scheduled Daily Job)

Runs at: 00:10 AM (server time)

Steps:

1. Lock processing date
2. Load all financial sources
3. Validate data integrity
4. Perform aggregations
5. Verify calculations
6. Persist snapshot
7. Mark day as closed

All operations must be atomic.

--------------------------------------------------
VALIDATION RULES
--------------------------------------------------

- snapshotDate must be unique
- revenue >= 0
- profit may be negative (loss allowed)
- activePayingShops >= 0
- BigDecimal scale must be enforced
- No null monetary fields
- implement validation in all setter
   - Use validaton support function from utils/ValidationUtils.java
   - validation function not exist inside ValidatoinUtils.java create it inside the ValidationUtils.java and that function must become another support function

All validation must be centralized.

--------------------------------------------------
STATE MANAGEMENT
--------------------------------------------------

SnapshotStatus:
   - DRAFT
   - FINALIZED
   - LOCKED

Rules:
   - Only DRAFT can be recalculated
   - FINALIZED is read-only
   - LOCKED is audit-protected

--------------------------------------------------
BUSINESS SERVICES
--------------------------------------------------

PlatformRevenueService
   - generateDailySnapshot()
   - getSnapshotByDate()
   - getRangeSummary()
   - getYearlyReport()

AggregationService
   - aggregateSubscriptions()
   - aggregateCommissions()
   - aggregateCosts()

AuditService
   - verifySnapshot()
   - validateSources()
   - logGeneration()

--------------------------------------------------
QUERY STRATEGY
--------------------------------------------------

Daily Report:
   SELECT *
   FROM platform_revenue_snapshots
   WHERE snapshot_date = ?

Monthly Summary:
   SELECT SUM(total_subscription_revenue),
          SUM(platform_net_profit)
   FROM platform_revenue_snapshots
   WHERE snapshot_date BETWEEN ? AND ?

--------------------------------------------------
ROLE ACCESS CONTROL
--------------------------------------------------

ADMIN:
   - Read all snapshots
   - Generate reports
   - Trigger recalculation (DRAFT only)

SYSTEM:
   - Generate snapshots
   - Lock periods

USER:
   - No access

--------------------------------------------------
AUDIT & COMPLIANCE
--------------------------------------------------

- All snapshot generations logged
- Source tables recorded
- Hash checksum stored
- Regeneration history preserved
- Immutable archives

--------------------------------------------------
ERROR HANDLING
--------------------------------------------------

- Missing source data → abort
- Duplicate date → reject
- Invalid totals → rollback
- Cost service unavailable → retry

--------------------------------------------------
SCALABILITY RULES
--------------------------------------------------

- Partition by year
- Archive old snapshots
- Batch aggregation
- Async preloading
- Horizontal job scaling

--------------------------------------------------
CODE QUALITY
--------------------------------------------------

- Clean Architecture
- Domain-driven design
- No logic in controllers
- Dedicated aggregation layer
- Centralized validation
- equals/hashCode via UUID
- JavaDoc on public APIs
- Testable calculation modules

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

Generate a secure, auditable, and scalable platform revenue engine that supports:

- Daily profit tracking
- Tax reporting
- Investor dashboards
- Cost analysis
- Long-term forecasting
- Financial compliance

The result must represent enterprise-grade financial infrastructure, not a simple reporting utility.
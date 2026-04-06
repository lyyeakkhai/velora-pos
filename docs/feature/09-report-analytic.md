# Feature 9: Reporting & Analytics

## System Overview

This system generates immutable, high-performance analytical data for financial reporting, seller performance, category trends, and AI-driven insights through scheduled aggregation jobs.

## Domain Model

### Entities

#### DailyProductSnapshot
Atomic-level product analytics.

| Field | Type | Validation |
|-------|------|------------|
| snapshotId | UUID | Immutable |
| snapshotDate | LocalDate | Immutable, one per shop/day |
| productId | UUID | FK |
| variantId | UUID | FK |
| sellerId | UUID | FK |
| categoryId | UUID | FK |
| shopId | UUID | FK, RAS |
| qtySold | int | >= 0 |
| baseCostPrice | BigDecimal | scale=2, HALF_UP |
| unitSalePrice | BigDecimal | scale=2, HALF_UP |
| stockAtMidnight | int | >= 0 |
| createdAt | LocalDateTime | Immutable |

#### DailyCategorySnapshot
Middle-level category analytics.

| Field | Type | Validation |
|-------|------|------------|
| snapshotId | UUID | PK |
| snapshotDate | LocalDate | One per category/shop/day |
| categoryId | UUID | FK |
| shopId | UUID | FK, RAS |
| catGrossRevenue | BigDecimal | scale=2, HALF_UP |
| catNetProfit | BigDecimal | scale=2, HALF_UP |
| catItemsSold | int | >= 0 |
| createdAt | LocalDateTime | Managed |

#### DailySnapshot
High-level shop analytics.

| Field | Type | Validation |
|-------|------|------------|
| snapshotId | UUID | PK |
| snapshotDate | LocalDate | One per shop/day |
| orgId | UUID | FK |
| shopId | UUID | FK, RAS |
| totalGross | BigDecimal | scale=2, HALF_UP |
| totalProfit | BigDecimal | scale=2, HALF_UP |
| orderCount | int | >= 0 |
| createdAt | LocalDateTime | Managed |

### AI Integration DTO

| Field | Type | Description |
|-------|------|-------------|
| metricName | String | Metric identifier |
| previousValue | BigDecimal | Prior period value |
| currentValue | BigDecimal | Current period value |
| changePercent | BigDecimal | Percentage change |
| riskLevel | RiskLevel | LOW/MEDIUM/HIGH |
| recommendation | String | AI recommendation |

## Entity Relationships

```
Shop (1:N) DailySnapshot
Shop (1:N) DailyCategorySnapshot
Shop (1:N) DailyProductSnapshot
Category (1:N) DailyCategorySnapshot
Product (1:N) DailyProductSnapshot
```

## Business Rules

1. All snapshots are write-once (no update/delete)
2. One snapshot per shop per day
3. All financial fields >= 0 with BigDecimal scale=2, HALF_UP
4. All snapshots scoped by shopId (row-level security)
5. All dates normalized to UTC
6. No negative revenue or profit (unless verified loss)

## State Machine

Snapshots are immutable once created - no state transitions.

## Implementation Details

### Aggregation Pipeline (DailyAnalyticsJob)

Runs once per day at 00:05 UTC:

1. Lock aggregation window (yesterday)
2. Fetch confirmed orders
3. Fetch order items
4. Fetch inventory history
5. Fetch seller assignments
6. Calculate aggregates
7. Persist snapshots
8. Mark job completed
9. Release lock

Idempotency: Job must be re-runnable, if snapshot exists → skip

### Calculation Rules

**Product Snapshot:**
- qtySold = SUM(orderItem.quantity)
- unitSalePrice = AVG(orderItem.soldPrice)
- stockAtMidnight = inventorySnapshot.stock

**Category Snapshot:**
- catGrossRevenue = SUM(qtySold * unitSalePrice)
- catNetProfit = SUM((unitSalePrice - baseCostPrice) * qtySold)
- catItemsSold = SUM(qtySold)

**Daily Snapshot:**
- totalGross = SUM(catGrossRevenue)
- totalProfit = SUM(catNetProfit)
- orderCount = COUNT(distinct orderId)

### Business Services

| Service | Methods |
|---------|---------|
| AnalyticsAggregationService | runDailyAggregation(), validateSnapshotUniqueness(), persistSnapshots() |
| ReportingService | getDailyReport(), getWeeklyReport(), getMonthlyReport(), getAnnualReport() |
| SellerAnalyticsService | rankSellers(), getSellerPerformance() |
| CategoryAnalyticsService | getCategoryTrends(), comparePeriods() |
| ForecastService | predictOutOfStock(), detectRevenueDrop() |

### Role Access Control

| Role | Access |
|------|-------|
| OWNER | Full analytics |
| MANAGER | Category/seller analytics |
| SELLER | Personal performance only |

## OOP Best Practices Applied

1. **Immutability** - All snapshots write-once, no update/delete
2. **BigDecimal** - All monetary values scale=2, HALF_UP
3. **Enums** - Risk levels use enums
4. **Row-Level Access Security** - All queries scoped by shopId
5. **Strategy Pattern** - Report strategies (Daily, Weekly, Monthly, Annual), Forecast strategies
6. **Records** - DTOs use Java records with validation
7. **Exception Handling** - Uses DomainException, IllegalStateException, IllegalArgumentException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `report_analyticModule/domain/DailySnapshot.java` | Shop-level analytics |
| `report_analyticModule/domain/DailyCategorySnapshot.java` | Category-level analytics |
| `report_analyticModule/domain/DailyProductSnapshot.java` | Product-level analytics |
| `report_analyticModule/domain/AnalyticsAggregationService.java` | Aggregation orchestration |
| `report_analyticModule/domain/ReportingService.java` | Report generation |
| `report_analyticModule/domain/ForecastService.java` | Predictive analytics |
| `report_analyticModule/domain/SellerAnalyticsService.java` | Seller performance tracking |
| `report_analyticModule/domain/CategoryAnalyticsService.java` | Category trend analysis |
| `report_analyticModule/domain/AnalyticsAccessPolicy.java` | Role-based access control |
| `report_analyticModule/domain/AnalyticsMoney.java` | Money validation utility |
| `common/DomainException.java` | Base business exception |

### Exception Handling

The module uses three types of exceptions:

| Exception Type | Usage |
|---------------|-------|
| `DomainException` | Access control violations (role, ownership) |
| `IllegalStateException` | Business rules (duplicate snapshot, negative revenue) |
| `IllegalArgumentException` | Parameter validation (null, range, negative values) |

```
RuntimeException
└── DomainException (access control)
    ├── "SUPER_ADMIN role required for: X"
    ├── "OWNER role required"
    ├── "MANAGER role required"
    ├── "Unknown operation: X"
    └── "SELLER can access personal performance only"

IllegalStateException (business rules)
    ├── "Duplicate daily snapshot prohibited"
    ├── "Negative revenue prohibited"
    └── "Negative profit requires verified loss"

IllegalArgumentException (validation)
    ├── "X must not be null"
    ├── "X must be >= 0"
    ├── "X must be > 0"
    ├── "startInclusive must be <= endInclusive"
    └── "days must be > 0"
```

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: High - Full entity implementation with strategy pattern and RBAC  
**Last Updated**: 2026-04-07
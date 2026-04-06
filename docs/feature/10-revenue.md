# Feature 10: Revenue Tracking

## System Overview

This system represents the financial "source of truth" for the platform. It tracks daily platform revenue, calculates net profit, and supports taxation, auditing, and investor reporting.

## Domain Model

### Entities

#### PlatformRevenueSnapshot
Daily platform financial aggregate.

| Field | Type | Validation |
|-------|------|------------|
| platformSnapId | UUID | Immutable |
| snapshotDate | LocalDate | Unique |
| totalSubscriptionRevenue | BigDecimal | scale=2 |
| platformNetProfit | BigDecimal | scale=2 (may be negative) |
| activePayingShops | Integer | >= 0 |
| createdAt | LocalDateTime | Immutable |

### Value Objects

| Value Object | Description |
|--------------|------------|
| SnapshotStatus | DRAFT, FINALIZED, LOCKED |

## Entity Relationships

```
PlatformRevenueSnapshot is a standalone aggregate
```

## Business Rules

1. snapshotDate is unique per day
2. revenue >= 0
3. profit may be negative (loss allowed)
4. activePayingShops >= 0
5. BigDecimal scale must be 2
6. No null monetary fields

## State Transitions

```
Snapshot: DRAFT → FINALIZED → LOCKED
Rules:
  - Only DRAFT can be recalculated
  - FINALIZED is read-only
  - LOCKED is audit-protected
```

## Implementation Details

### Data Sources

Platform revenue calculated from:

1. **Transactions** - Subscription payments
2. **PlatformRegistry** - Active shop subscriptions
3. **DailySnapshots** - Shop-level revenue
4. **InfrastructureCosts** - Server costs, API fees

### Calculation Rules

1. **Total Subscription Revenue**
   - SUM(all successful subscription payments for the day)

2. **Platform Net Profit**
   - totalSubscriptionRevenue + commissionRevenue - infrastructureCosts

3. **Active Paying Shops**
   - COUNT(DISTINCT shop_id) WHERE payment_status = PAID

### Snapshot Generation Pipeline (RevenueAggregationJob)

Runs daily at 00:10 UTC:

1. Lock processing date
2. Load all financial sources
3. Validate data integrity
4. Perform aggregations
5. Verify calculations
6. Persist snapshot
7. Mark day as closed

All operations must be atomic.

### Business Services

| Service | Methods |
|---------|---------|
| PlatformRevenueService | generateDailySnapshot(), getSnapshotByDate(), getRangeSummary(), getYearlyReport() |
| AggregationService | aggregateSubscriptions(), aggregateCommissions(), aggregateCosts() |
| AuditService | verifySnapshot(), validateSources(), logGeneration() |

### Role Access Control

| Role | Access |
|------|-------|
| ADMIN | Read all, generate reports, trigger recalculation (DRAFT only) |
| SYSTEM | Generate snapshots, lock periods |
| USER | No access |

## OOP Best Practices Applied

1. **Immutability** - platformSnapId and createdAt immutable
2. **Financial Accuracy** - BigDecimal scale=2, HALF_UP
3. **Append-Only** - No update/delete after finalization
4. **Auditability** - Traceable, reproducible calculations
5. **State Machine** - Snapshot status transitions enforced with validation
6. **Exception Handling** - Uses IllegalStateException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `payment/domain/PlatformRevenueSnapshot.java` | Daily revenue aggregate |
| `common/DomainException.java` | Base business exception |

### Exception Handling

| Exception Type | Usage |
|---------------|-------|
| `IllegalStateException` | State transitions (DRAFT/FINALIZED/LOCKED rules) |

```
IllegalStateException (state transitions)
    ├── "Can only set metrics for DRAFT snapshots"
    ├── "Can only finalize DRAFT snapshots"
    ├── "Cannot finalize snapshot without metrics"
    └── "Can only lock FINALIZED snapshots"
```

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: High - Full entity with state machine  
**Last Updated**: 2026-04-07
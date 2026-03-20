# Inheritance Implementation Progress

Track the application of inheritance across all domain entities and services.
Update this file after each implementation step.

---

## What is Inheritance in This Project?

Inheritance is used to:
- Share common fields (id, createdAt, updatedAt, status) across entities via abstract base classes
- Share common validation behavior via abstract service base classes
- Reduce duplication in subscription lifecycle entities (UserAccount, ShopAccount share the same lifecycle logic)
- Share snapshot structure across DailyProductSnapshot, DailyCategorySnapshot, DailySnapshot

---

## Planned Inheritance Hierarchy

```
AbstractEntity
├── AbstractAuditableEntity (+ createdAt, updatedAt)
│   ├── User
│   ├── UserAuth
│   ├── Membership
│   ├── Shop
│   ├── Product
│   ├── ProductVariant
│   ├── Order
│   ├── Notification
│   └── FeatureSuggestion
│
AbstractSubscriptionAccount (+ subscriptionId, planId, registryId, status, startDate, endDate, refundDeadline)
├── UserAccount
└── ShopAccount
│
AbstractSubscriptionRecord (+ subscriptionId, transactionId, planId, status, startDate, endDate, refundDeadline)
├── UserSubscription
└── ShopSubscription
│
AbstractSnapshot (+ snapshotId, snapshotDate, shopId, createdAt)
├── DailyProductSnapshot
├── DailyCategorySnapshot
└── DailySnapshot
│
AbstractDomainService (+ validateActor, requireRole)
├── NotificationService
├── FeedbackService
├── ReportingService
└── AnalyticsAggregationService
```

---

## Implementation Tasks

### Step 1 — AbstractEntity Base Class
- [ ] Create `AbstractEntity` with `id` (UUID) field
- [ ] Add `validateId()` using `ValidationUtils.validateUUID()`
- [ ] Override `equals()` and `hashCode()` based on `id`
- [ ] Override `toString()` with class name + id

**Status:** Not started
**File:** `src/main/java/com/velora/app/common/AbstractEntity.java`

---

### Step 2 — AbstractAuditableEntity
- [ ] Extend `AbstractEntity`
- [ ] Add `createdAt` (LocalDateTime, immutable, auto-set in constructor)
- [ ] Add `updatedAt` (LocalDateTime, auto-managed via `touch()`)
- [ ] Add `protected void touch()` to update `updatedAt`

**Status:** Not started
**File:** `src/main/java/com/velora/app/common/AbstractAuditableEntity.java`

---

### Step 3 — Apply to Auth Domain
- [ ] `User` extends `AbstractAuditableEntity` — remove duplicate id/timestamp fields
- [ ] `UserAuth` extends `AbstractAuditableEntity` — remove duplicate createdAt
- [ ] `Membership` extends `AbstractAuditableEntity` — remove duplicate timestamps, use `touch()`
- [ ] `Role` extends `AbstractEntity` — remove duplicate roleId/equals/hashCode

**Status:** Not started
**Files:** `auth/User.java`, `auth/UserAuth.java`, `auth/Membership.java`, `auth/Role.java`

---

### Step 4 — AbstractSubscriptionAccount
- [ ] Create `AbstractSubscriptionAccount` with shared fields:
  - `subscriptionId`, `planId`, `registryId`, `status`, `startDate`, `endDate`, `refundDeadline`, `currentPlanDurationMonths`
- [ ] Add shared methods: `calculateEndDate()`, `calculateRefundDeadline()`, `markExpiredIfNeeded()`, `checkExpiration()`
- [ ] Add abstract method: `isActive()`

**Status:** Not started
**File:** `src/main/java/com/velora/app/common/AbstractSubscriptionAccount.java`

---

### Step 5 — Apply to Subscription Domain
- [ ] `UserAccount` extends `AbstractSubscriptionAccount` — remove duplicate fields and shared methods
- [ ] `ShopAccount` extends `AbstractSubscriptionAccount` — remove duplicate fields and shared methods
- [ ] Keep domain-specific methods: `activatePlan()`, `upgrade()`, `renew()`, `cancel()`, `expire()`

**Status:** Not started
**Files:** `plan_subscription/UserAccount.java`, `plan_subscription/ShopAccount.java`

---

### Step 6 — AbstractSubscriptionRecord
- [ ] Create `AbstractSubscriptionRecord` with shared fields:
  - `subscriptionId`, `transactionId`, `planId`, `status`, `startDate`, `endDate`, `refundDeadline`
- [ ] Add shared method: `markRefunded()`

**Status:** Not started
**File:** `src/main/java/com/velora/app/common/AbstractSubscriptionRecord.java`

---

### Step 7 — Apply to Subscription Records
- [ ] `UserSubscription` extends `AbstractSubscriptionRecord`
- [ ] `ShopSubscription` extends `AbstractSubscriptionRecord`

**Status:** Not started
**Files:** `plan_subscription/UserSubscription.java`, `plan_subscription/ShopSubscription.java`

---

### Step 8 — AbstractSnapshot
- [ ] Create `AbstractSnapshot` with shared fields:
  - `snapshotId` (UUID, immutable), `snapshotDate` (LocalDate, immutable), `shopId` (UUID), `createdAt` (LocalDateTime, immutable)
- [ ] Enforce write-once (no setters for immutable fields)

**Status:** Not started
**File:** `src/main/java/com/velora/app/common/AbstractSnapshot.java`

---

### Step 9 — Apply to Analytics Domain
- [ ] `DailyProductSnapshot` extends `AbstractSnapshot`
- [ ] `DailyCategorySnapshot` extends `AbstractSnapshot`
- [ ] `DailySnapshot` extends `AbstractSnapshot`

**Status:** Not started
**Files:** `report-and-analytic/DailyProductSnapshot.java`, `DailyCategorySnapshot.java`, `DailySnapshot.java`

---

### Step 10 — AbstractDomainService
- [ ] Create `AbstractDomainService` with:
  - `protected void requireRole(Role.RoleName actual, Role.RoleName... allowed)`
  - `protected void requireNotNull(Object value, String fieldName)`
- [ ] Apply to `NotificationService`, `FeedbackService`, `ReportingService`

**Status:** Not started
**File:** `src/main/java/com/velora/app/common/AbstractDomainService.java`

---

## Progress Summary

| Step | Description | Status |
|---|---|---|
| 1 | AbstractEntity | Not started |
| 2 | AbstractAuditableEntity | Not started |
| 3 | Auth domain | Not started |
| 4 | AbstractSubscriptionAccount | Not started |
| 5 | UserAccount + ShopAccount | Not started |
| 6 | AbstractSubscriptionRecord | Not started |
| 7 | UserSubscription + ShopSubscription | Not started |
| 8 | AbstractSnapshot | Not started |
| 9 | Analytics snapshots | Not started |
| 10 | AbstractDomainService | Not started |

---

## Update Log

> Add an entry here each time you complete a step.

- [ ] Step 1 completed — date: ___
- [ ] Step 2 completed — date: ___
- [ ] Step 3 completed — date: ___
- [ ] Step 4 completed — date: ___
- [ ] Step 5 completed — date: ___
- [ ] Step 6 completed — date: ___
- [ ] Step 7 completed — date: ___
- [ ] Step 8 completed — date: ___
- [ ] Step 9 completed — date: ___
- [ ] Step 10 completed — date: ___

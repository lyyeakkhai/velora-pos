# Polymorphism Implementation Progress

Track the application of polymorphism across all domain services, dispatch pipelines, and strategy patterns.
Update this file after each implementation step.

---

## What is Polymorphism in This Project?

Polymorphism is used to:
- Allow `DispatchService` to send notifications through multiple channels (IN_APP, EMAIL) via a common `NotificationChannel` strategy
- Allow `DiscountService` to apply different discount types (PERCENTAGE, FIXED) via a common `DiscountStrategy` interface
- Allow `PlatformRegistry` to route activation to either `UserAccount` or `ShopAccount` via a common `SubscriptionAccount` interface
- Allow `AnalyticsAggregationService` to process different snapshot types via a common `SnapshotAggregator` interface
- Allow `ReportingService` to generate different report periods via a common `ReportPeriod` strategy
- Allow `PaymentIntent` to support different payment methods via a common `PaymentProcessor` interface

---

## Planned Polymorphism Patterns

```
NotificationDispatcher
└── NotificationSender (interface)
    ├── InAppNotificationSender
    └── EmailNotificationSender

DiscountEngine
└── DiscountStrategy (interface)
    ├── PercentageDiscountStrategy
    └── FixedDiscountStrategy

SubscriptionActivationRouter
└── SubscriptionAccount (interface)
    ├── UserAccount
    └── ShopAccount

SnapshotAggregator (interface)
├── ProductSnapshotAggregator
├── CategorySnapshotAggregator
└── DailySnapshotAggregator

ReportPeriodStrategy (interface)
├── DailyReportStrategy
├── WeeklyReportStrategy
├── MonthlyReportStrategy
└── AnnualReportStrategy

PaymentProcessor (interface)
├── CardPaymentProcessor
└── QrCodePaymentProcessor
```

---

## Implementation Tasks

### Step 1 — NotificationSender Interface
- [ ] Create `NotificationSender` interface:
  ```java
  public interface NotificationSender {
      NotificationChannel getChannel();
      boolean canSend(Notification notification, NotificationPreferences preferences);
      void send(Notification notification);
  }
  ```
- [ ] Implement `InAppNotificationSender` — always sends, records dispatch record
- [ ] Implement `EmailNotificationSender` — sends only if HIGH priority + emailEnabled
- [ ] Update `DispatchService` to accept `List<NotificationSender>` and iterate

**Status:** Not started
**Files:**
- `notification/NotificationSender.java`
- `notification/InAppNotificationSender.java`
- `notification/EmailNotificationSender.java`
- `notification/DispatchService.java` (update)

---

### Step 2 — DiscountStrategy Interface
- [ ] Create `DiscountStrategy` interface:
  ```java
  public interface DiscountStrategy {
      DiscountType getType();
      BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue);
      void validate(BigDecimal discountValue);
  }
  ```
- [ ] Implement `PercentageDiscountStrategy` — applies `basePrice * (1 - discountValue/100)`, validates 0–100
- [ ] Implement `FixedDiscountStrategy` — applies `basePrice - discountValue`, validates discountValue ≤ basePrice
- [ ] Update `DiscountService.calculateFinalPrice()` to use strategy lookup by `DiscountType`

**Status:** Not started
**Files:**
- `inventory-event-management/DiscountStrategy.java`
- `inventory-event-management/PercentageDiscountStrategy.java`
- `inventory-event-management/FixedDiscountStrategy.java`
- `inventory-event-management/DiscountService.java` (update)

---

### Step 3 — SubscriptionAccount Interface
- [ ] Create `SubscriptionAccount` interface:
  ```java
  public interface SubscriptionAccount {
      UUID getSubscriptionId();
      UUID getPlanId();
      UUID getRegistryId();
      void activatePlan(SubscriptionPlan plan);
      void expire();
      void cancel();
      boolean isActive();
      void markExpiredIfNeeded();
  }
  ```
- [ ] `UserAccount` implements `SubscriptionAccount`
- [ ] `ShopAccount` implements `SubscriptionAccount`
- [ ] Create `SubscriptionActivationRouter`:
  ```java
  public class SubscriptionActivationRouter {
      public SubscriptionAccount route(TargetType targetType,
          UserAccount userAccount, ShopAccount shopAccount);
  }
  ```
- [ ] Update `PlanSubscriptionEngine` to use `SubscriptionActivationRouter`

**Status:** Not started
**Files:**
- `plan_subscription/SubscriptionAccount.java`
- `plan_subscription/SubscriptionActivationRouter.java`
- `plan_subscription/PlanSubscriptionEngine.java` (update)
- `plan_subscription/UserAccount.java` (add implements)
- `plan_subscription/ShopAccount.java` (add implements)

---

### Step 4 — SnapshotAggregator Interface
- [ ] Create `SnapshotAggregator` interface:
  ```java
  public interface SnapshotAggregator<T> {
      String getAggregatorName();
      boolean alreadyExists(UUID shopId, LocalDate date);
      T aggregate(UUID shopId, LocalDate date);
      void persist(T snapshot);
  }
  ```
- [ ] Implement `ProductSnapshotAggregator` — aggregates from order items + inventory
- [ ] Implement `CategorySnapshotAggregator` — aggregates from product snapshots
- [ ] Implement `DailySnapshotAggregator` — aggregates from category snapshots
- [ ] Update `AnalyticsAggregationService.runDailyAggregation()` to iterate aggregators in order

**Status:** Not started
**Files:**
- `report-and-analytic/SnapshotAggregator.java`
- `report-and-analytic/ProductSnapshotAggregator.java`
- `report-and-analytic/CategorySnapshotAggregator.java`
- `report-and-analytic/DailySnapshotAggregator.java`
- `report-and-analytic/AnalyticsAggregationService.java` (update)

---

### Step 5 — ReportPeriodStrategy Interface
- [ ] Create `ReportPeriodStrategy` interface:
  ```java
  public interface ReportPeriodStrategy {
      DateRange getDateRange(LocalDate endDate);
      String getPeriodName();
      boolean requiresOwnerRole();
  }
  ```
- [ ] Implement `DailyReportStrategy` — range = single day
- [ ] Implement `WeeklyReportStrategy` — range = endDate - 6 days to endDate
- [ ] Implement `MonthlyReportStrategy` — range = first day of month to endDate
- [ ] Implement `AnnualReportStrategy` — range = first day of year to endDate, requires OWNER
- [ ] Update `ReportingService` to accept `ReportPeriodStrategy` and use it

**Status:** Not started
**Files:**
- `report-and-analytic/ReportPeriodStrategy.java`
- `report-and-analytic/DailyReportStrategy.java`
- `report-and-analytic/WeeklyReportStrategy.java`
- `report-and-analytic/MonthlyReportStrategy.java`
- `report-and-analytic/AnnualReportStrategy.java`
- `report-and-analytic/ReportingService.java` (update)

---

### Step 6 — PaymentProcessor Interface
- [ ] Create `PaymentProcessor` interface:
  ```java
  public interface PaymentProcessor {
      CardType getSupportedCardType(); // null for QR
      PaymentIntent createIntent(Transaction transaction);
      boolean verify(String gatewayRef, BigDecimal amount);
  }
  ```
- [ ] Implement `CardPaymentProcessor` — handles VISA, MASTERCARD, AMEX
- [ ] Implement `QrCodePaymentProcessor` — handles QR code generation and verification
- [ ] Update `PaymentService` to use processor lookup by payment method type

**Status:** Not started
**Files:**
- `payment/PaymentProcessor.java`
- `payment/CardPaymentProcessor.java`
- `payment/QrCodePaymentProcessor.java`
- `payment/PaymentService.java` (update — in service layer)

---

### Step 7 — ForecastStrategy Interface
- [ ] Create `ForecastStrategy` interface:
  ```java
  public interface ForecastStrategy {
      String getForecastType();
      List<OutOfStockPredictionDTO> predict(List<DailyProductSnapshot> snapshots);
  }
  ```
- [ ] Implement `LinearTrendForecastStrategy` — simple linear regression on qtySold
- [ ] Implement `MovingAverageForecastStrategy` — 7-day moving average
- [ ] Update `ForecastService` to accept configurable strategy

**Status:** Not started
**Files:**
- `report-and-analytic/ForecastStrategy.java`
- `report-and-analytic/LinearTrendForecastStrategy.java`
- `report-and-analytic/MovingAverageForecastStrategy.java`
- `report-and-analytic/ForecastService.java` (update)

---

### Step 8 — Role-Based Access Polymorphism
- [ ] Create `AccessPolicy` interface:
  ```java
  public interface AccessPolicy {
      void check(Role.RoleName actorRole, String operation);
  }
  ```
- [ ] Implement `AnalyticsAccessPolicy` (already exists — refactor to implement interface)
- [ ] Implement `FeedbackAccessPolicy` (already exists — refactor to implement interface)
- [ ] Implement `NotificationAccessPolicy` (already exists — refactor to implement interface)
- [ ] Implement `InventoryRolePolicy` (already exists as `RolePolicy` — refactor)

**Status:** Not started
**Files:**
- `common/AccessPolicy.java`
- `report-and-analytic/AnalyticsAccessPolicy.java` (update)
- `feedback/FeedbackAccessPolicy.java` (update)
- `notification/NotificationAccessPolicy.java` (update)
- `inventory-event-management/RolePolicy.java` (update)

---

## Progress Summary

| Step | Description | Status |
|---|---|---|
| 1 | NotificationSender polymorphism | Not started |
| 2 | DiscountStrategy polymorphism | Not started |
| 3 | SubscriptionAccount interface + router | Not started |
| 4 | SnapshotAggregator polymorphism | Not started |
| 5 | ReportPeriodStrategy polymorphism | Not started |
| 6 | PaymentProcessor polymorphism | Not started |
| 7 | ForecastStrategy polymorphism | Not started |
| 8 | AccessPolicy polymorphism | Not started |

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

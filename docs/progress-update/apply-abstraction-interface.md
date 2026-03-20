# Abstraction & Interface Implementation Progress

Track the application of abstraction and interfaces across all domain repositories, services, and gateways.
Update this file after each implementation step.

---

## What is Abstraction in This Project?

Abstraction is used to:
- Define repository contracts (interfaces) that the domain depends on, with infrastructure implementations
- Define service contracts so the application layer depends on abstractions, not concretions
- Define gateway interfaces (EmailGateway, WebhookSecurity) to decouple infrastructure
- Define store interfaces for all domain persistence operations
- Enable testability by allowing mock implementations in tests

---

## Planned Interface Hierarchy

```
Repository Interfaces (domain layer)
├── ShopRepository
├── ProductStore
├── ProductVariantStore
├── CategoryStore
├── EventTypeStore
├── EventProductStore
├── OrderStore
├── ReceiptStore
├── DeliveryStore
├── PaymentIntentStore
├── NotificationRepository
├── NotificationDispatchRepository
├── NotificationPreferencesRepository
├── FeatureSuggestionRepository
├── DailySnapshotRepository
├── DailyProductSnapshotRepository
├── DailyCategorySnapshotRepository
├── AnalyticsJobLockStore
├── AnalyticsJobLogStore
├── AnalyticsJobRunStore
├── ConfirmedOrderReadRepository
├── InventoryAnalyticsReadRepository
└── OrderItemReadRepository

Gateway Interfaces (domain layer)
├── EmailGateway
├── WebhookSecurity
└── TransactionRunner

Service Interfaces (application layer)
├── IAuthService
├── ISubscriptionService
├── IStoreService
├── IInventoryManagementService
├── ISaleOrchestrationService
├── IPaymentService
├── INotificationOrchestrationService
├── IFeedbackOrchestrationService
├── IAnalyticsService
├── IRevenueService
└── IAdminService
```

---

## Implementation Tasks

### Step 1 — Repository Interfaces: Auth Domain
- [ ] `UserRepository` — save(User), findById(UUID), findByUsername(String), existsByUsername(String)
- [ ] `UserAuthRepository` — save(UserAuth), findByEmail(String), findByUserId(UUID), existsByEmail(String)
- [ ] `MembershipRepository` — save(Membership), findByUserId(UUID), findByShopId(UUID), findByUserAndShop(UUID, UUID)
- [ ] `RoleRepository` — findByRoleName(RoleName), findById(UUID)

**Status:** Not started
**Package:** `com.velora.app.core.domain.auth`

---

### Step 2 — Repository Interfaces: Subscription Domain
- [ ] `SubscriptionPlanRepository` — save(SubscriptionPlan), findById(UUID), findBySlug(String), findAllActive()
- [ ] `FeatureRepository` — save(Feature), findByKey(String), findAll()
- [ ] `PlatformRegistryRepository` — save(PlatformRegistry), findById(UUID), findByOwnerId(UUID)
- [ ] `UserAccountRepository` — save(UserAccount), findByUserId(UUID), findAllActive()
- [ ] `ShopAccountRepository` — save(ShopAccount), findByShopId(UUID), findAllActive()
- [ ] `UserSubscriptionRepository` — save(UserSubscription), findByUserId(UUID)
- [ ] `ShopSubscriptionRepository` — save(ShopSubscription), findByShopId(UUID)

**Status:** Not started
**Package:** `com.velora.app.core.domain.plan_subscription`

---

### Step 3 — Repository Interfaces: Store Domain
- [ ] `ShopRepository` (already exists) — verify: save(Shop), findById(UUID), findBySlug(String), findByOwnerId(UUID)
- [ ] `ShopSettingsRepository` — save(ShopSettings), findByShopId(UUID)

**Status:** Not started
**Package:** `com.velora.app.core.domain.store-management`

---

### Step 4 — Repository Interfaces: Inventory Domain
- [ ] `ProductStore` (already exists) — verify: save(Product), findById(UUID), findByShopId(UUID)
- [ ] `ProductVariantStore` (already exists) — verify: save(ProductVariant), findByProductId(UUID), findBySku(String)
- [ ] `CategoryStore` (already exists) — verify: save(Category), findByShopId(UUID)
- [ ] `EventTypeStore` (already exists) — verify: save(EventType), findByShopId(UUID)
- [ ] `EventProductStore` (already exists) — verify: save(EventProduct), findByEventId(UUID), findByProductId(UUID)

**Status:** Not started
**Package:** `com.velora.app.core.domain.inventory-event-management`

---

### Step 5 — Repository Interfaces: Sale Domain
- [ ] `OrderStore` (already exists) — verify: save(Order), findById(UUID), findByShopId(UUID)
- [ ] `ReceiptStore` (already exists) — verify: save(Receipt), findByOrderId(UUID)
- [ ] `DeliveryStore` (already exists) — verify: save(Delivery), findByOrderId(UUID)
- [ ] `PaymentIntentStore` (already exists) — verify: save(PaymentIntent), findByBankRefId(String), getForUpdate(UUID), existsByBankRefId(String), delete(UUID)

**Status:** Not started
**Package:** `com.velora.app.core.domain.sale-management`

---

### Step 6 — Repository Interfaces: Payment Domain
- [ ] `PaymentMethodRepository` — save(PaymentMethod), findById(UUID)
- [ ] `TransactionRepository` — save(Transaction), findById(UUID), findByGatewayRef(String)
- [ ] `InvoiceRepository` — save(Invoice), findById(UUID), findByTransactionId(UUID)
- [ ] `PlatformRevenueSnapshotRepository` — save(PlatformRevenueSnapshot), findByDate(LocalDate)

**Status:** Not started
**Package:** `com.velora.app.core.domain.payment`

---

### Step 7 — Repository Interfaces: Notification Domain
- [ ] `NotificationRepository` (already exists) — verify: append(Notification), markRead(UUID, UUID), markAllRead(UUID), countUnread(UUID), findUserNotifications(UUID, int, LocalDateTime)
- [ ] `NotificationDispatchRepository` (already exists) — verify: createIfAbsent(NotificationDispatchRecord), findPending(), save(NotificationDispatchRecord)
- [ ] `NotificationPreferencesRepository` (already exists) — verify: save(NotificationPreferences), findByUserId(UUID)

**Status:** Not started
**Package:** `com.velora.app.core.domain.notification`

---

### Step 8 — Repository Interfaces: Feedback Domain
- [ ] `FeatureSuggestionRepository` (already exists) — verify: save(FeatureSuggestion), findById(UUID), findByUserId(UUID), findByStatus(SuggestionStatus)

**Status:** Not started
**Package:** `com.velora.app.core.domain.feedback`

---

### Step 9 — Repository Interfaces: Analytics Domain
- [ ] `DailySnapshotRepository` (already exists) — verify: save(DailySnapshot), findByShopAndDate(UUID, LocalDate), findByShopAndDateRange(UUID, LocalDate, LocalDate)
- [ ] `DailyProductSnapshotRepository` (already exists) — verify: save(DailyProductSnapshot), findByShopAndDate(UUID, LocalDate)
- [ ] `DailyCategorySnapshotRepository` (already exists) — verify: save(DailyCategorySnapshot), findByShopAndDate(UUID, LocalDate)
- [ ] `AnalyticsJobLockStore` (already exists) — verify: tryLock(LocalDate), release(LocalDate)
- [ ] `AnalyticsJobRunStore` (already exists) — verify: save(AnalyticsJobRun), findByDate(LocalDate)
- [ ] `ConfirmedOrderReadRepository` (already exists) — verify: findConfirmedOrdersByDate(LocalDate)
- [ ] `InventoryAnalyticsReadRepository` (already exists) — verify: findStockAtMidnight(LocalDate)
- [ ] `OrderItemReadRepository` (already exists) — verify: findOrderItemsByDate(LocalDate)

**Status:** Not started
**Package:** `com.velora.app.core.domain.report-and-analytic`

---

### Step 10 — Gateway Interfaces
- [ ] `EmailGateway` (already exists) — verify: send(String to, String subject, String body)
- [ ] `WebhookSecurity` (already exists) — verify: verifySignature(String payload, String signature), verifyNotReplayed(String nonce)
- [ ] `TransactionRunner` (already exists) — verify: runInTransaction(Runnable task)

**Status:** Not started
**Packages:** `notification/`, `sale-management/`

---

### Step 11 — Service Interfaces (Application Layer)
- [ ] `IAuthService` — interface for `AuthService`
- [ ] `ISubscriptionService` — interface for `SubscriptionService`
- [ ] `IStoreService` — interface for `StoreService`
- [ ] `IInventoryManagementService` — interface for `InventoryManagementService`
- [ ] `ISaleOrchestrationService` — interface for `SaleOrchestrationService`
- [ ] `IPaymentService` — interface for `PaymentService`
- [ ] `INotificationOrchestrationService` — interface for `NotificationOrchestrationService`
- [ ] `IFeedbackOrchestrationService` — interface for `FeedbackOrchestrationService`
- [ ] `IAnalyticsService` — interface for `AnalyticsService`
- [ ] `IRevenueService` — interface for `RevenueService`
- [ ] `IAdminService` — interface for `AdminService`

**Status:** Not started
**Package:** `com.velora.app.core.service`

---

### Step 12 — Infrastructure Implementations
- [ ] `PostgresShopRepository` implements `ShopRepository` (already exists — verify)
- [ ] Create remaining Postgres implementations for all repository interfaces
- [ ] Each implementation lives in `infrastructure/db/`

**Status:** Partially started (PostgresShopRepository exists)
**Package:** `com.velora.app.infrastructure.db`

---

## Progress Summary

| Step | Description | Status |
|---|---|---|
| 1 | Auth repository interfaces | Not started |
| 2 | Subscription repository interfaces | Not started |
| 3 | Store repository interfaces | Not started |
| 4 | Inventory repository interfaces | Not started |
| 5 | Sale repository interfaces | Not started |
| 6 | Payment repository interfaces | Not started |
| 7 | Notification repository interfaces | Not started |
| 8 | Feedback repository interfaces | Not started |
| 9 | Analytics repository interfaces | Not started |
| 10 | Gateway interfaces | Not started |
| 11 | Service interfaces | Not started |
| 12 | Infrastructure implementations | In progress |

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
- [ ] Step 11 completed — date: ___
- [ ] Step 12 completed — date: ___

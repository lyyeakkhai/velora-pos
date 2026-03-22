# Requirements Document

## Introduction

This document defines the requirements for applying OOP principles — Inheritance, Abstraction, Interfaces, and Polymorphism — systematically across all 11 domain modules of the Velora platform. Velora is a multi-tenant SaaS commerce platform built on Clean Architecture and Domain-Driven Design (DDD). The goal is to eliminate code duplication, enforce domain contracts, and enable extensible behavior through a structured four-phase OOP refactoring.

## Glossary

- **Velora_Platform**: The multi-tenant SaaS commerce platform being refactored
- **AbstractEntity**: Base class providing UUID identity, equals, hashCode, and toString
- **AbstractAuditableEntity**: Extends AbstractEntity with createdAt, updatedAt, and touch()
- **AbstractSubscriptionAccount**: Abstract base for UserAccount and ShopAccount with shared subscription lifecycle fields and methods
- **AbstractSubscriptionRecord**: Abstract base for UserSubscription and ShopSubscription with shared record fields
- **AbstractSnapshot**: Immutable base for all daily analytics snapshot entities
- **AbstractDomainService**: Base class providing requireRole and requireNotNull guard methods
- **AbstractAccessPolicy**: Abstract base for all role-based access policy classes
- **AbstractSnapshotAggregator**: Generic template-method base for snapshot aggregation pipelines
- **AbstractReportPeriod**: Abstract base for report period strategies with a concrete buildReport method
- **AbstractDiscountCalculator**: Abstract base for discount calculation with profit margin validation
- **AbstractNotificationDispatcher**: Abstract base for notification dispatch with shouldSend and dispatch template method
- **DomainException**: Runtime exception thrown for all domain-level business rule violations
- **Repository**: Interface defining persistence contract for a domain aggregate
- **Gateway**: Interface defining an external system integration contract (email, webhook, transaction)
- **IService**: Application-layer service interface defining the public contract for a use-case orchestrator
- **SnapshotAggregator**: Interface for pluggable snapshot aggregation strategies
- **DiscountStrategy**: Interface for pluggable discount calculation strategies
- **NotificationSender**: Interface for pluggable notification channel senders
- **ReportPeriodStrategy**: Interface for pluggable report period date-range strategies
- **PaymentProcessor**: Interface for pluggable payment method processors
- **ForecastStrategy**: Interface for pluggable out-of-stock prediction algorithms
- **AccessPolicy**: Interface for pluggable role-based access control checks
- **SubscriptionAccount**: Interface unifying UserAccount and ShopAccount for polymorphic activation routing
- **shopId**: UUID identifying the shop that owns a given domain record (row-level security scope)
- **touch()**: Protected method on AbstractAuditableEntity that sets updatedAt to the current timestamp

---

## Requirements

### Requirement 1: AbstractEntity Base Class

**User Story:** As a developer, I want all domain entities to share a common identity base class, so that UUID-based equality and identity are consistent across the entire domain.

#### Acceptance Criteria

1. THE AbstractEntity SHALL contain a single `UUID id` field that is set in the constructor and has no public setter.
2. THE AbstractEntity SHALL override `equals(Object)` and `hashCode()` based solely on the `id` field.
3. THE AbstractEntity SHALL override `toString()` returning a string in the format `ClassName{id=<uuid>}`.
4. WHEN two AbstractEntity instances have the same `id`, THE AbstractEntity SHALL consider them equal regardless of other field values.
5. IF a null `id` is passed to the constructor, THEN THE AbstractEntity SHALL throw a DomainException.

---

### Requirement 2: AbstractAuditableEntity Base Class

**User Story:** As a developer, I want auditable entities to automatically track creation and modification timestamps, so that audit trails are consistent and cannot be bypassed.

#### Acceptance Criteria

1. THE AbstractAuditableEntity SHALL extend AbstractEntity.
2. THE AbstractAuditableEntity SHALL contain a `LocalDateTime createdAt` field set once in the constructor with no public setter.
3. THE AbstractAuditableEntity SHALL contain a `LocalDateTime updatedAt` field managed exclusively via the `touch()` method.
4. WHEN `touch()` is called, THE AbstractAuditableEntity SHALL set `updatedAt` to `LocalDateTime.now()`.
5. THE AbstractAuditableEntity SHALL expose `createdAt` and `updatedAt` as read-only via getters.

---

### Requirement 3: Inheritance Applied to Auth Domain

**User Story:** As a developer, I want Auth domain entities to extend the appropriate abstract base classes, so that duplicate id and timestamp fields are eliminated.

#### Acceptance Criteria

1. THE User_Entity SHALL extend AbstractAuditableEntity and remove its own duplicate `id`, `createdAt`, and `updatedAt` fields.
2. THE UserAuth_Entity SHALL extend AbstractAuditableEntity and remove its own duplicate `createdAt` field.
3. THE Membership_Entity SHALL extend AbstractAuditableEntity, remove duplicate timestamp fields, and call `touch()` on any mutation method.
4. THE Role_Entity SHALL extend AbstractEntity and remove its own duplicate `roleId`, `equals()`, and `hashCode()` implementations.
5. WHEN a Membership is updated, THE Membership_Entity SHALL call `touch()` to record the modification timestamp.

---

### Requirement 4: Inheritance Applied to Store, Inventory, Sale, Payment, Notification, and Feedback Domains

**User Story:** As a developer, I want all core domain entities across Store, Inventory, Sale, Payment, Notification, and Feedback to extend AbstractAuditableEntity, so that id and timestamp duplication is eliminated platform-wide.

#### Acceptance Criteria

1. THE Shop_Entity SHALL extend AbstractAuditableEntity and call `touch()` inside `transitionStatus()` and `updateAddress()`.
2. THE Product_Entity, ProductVariant_Entity, Category_Entity, EventType_Entity, and EventProduct_Entity SHALL each extend AbstractAuditableEntity and remove duplicate `id`, `createdAt`, and `updatedAt` fields.
3. THE Order_Entity, OrderItem_Entity, Receipt_Entity, Delivery_Entity, and PaymentIntent_Entity SHALL each extend AbstractAuditableEntity and remove duplicate `id`, `createdAt`, and `updatedAt` fields.
4. THE Transaction_Entity, Invoice_Entity, and PaymentMethod_Entity SHALL each extend AbstractAuditableEntity and remove duplicate `id`, `createdAt`, and `updatedAt` fields.
5. THE Notification_Entity, NotificationPreferences_Entity, and NotificationDispatchRecord_Entity SHALL each extend AbstractAuditableEntity and remove duplicate `id`, `createdAt`, and `updatedAt` fields.
6. THE FeatureSuggestion_Entity SHALL extend AbstractAuditableEntity and call `touch()` inside `edit()` and `updateStatus()`.
7. WHEN any mutation method is called on an entity that extends AbstractAuditableEntity, THE entity SHALL call `touch()` to update `updatedAt`.

---

### Requirement 5: AbstractSubscriptionAccount

**User Story:** As a developer, I want UserAccount and ShopAccount to share a common subscription lifecycle base class, so that subscription state management logic is not duplicated.

#### Acceptance Criteria

1. THE AbstractSubscriptionAccount SHALL contain shared fields: `subscriptionId`, `planId`, `registryId`, `status`, `startDate`, `endDate`, `refundDeadline`, and `currentPlanDurationMonths`.
2. THE AbstractSubscriptionAccount SHALL provide a concrete `calculateEndDate(LocalDate start, int months)` method that returns `start.plusMonths(months)`.
3. THE AbstractSubscriptionAccount SHALL provide a concrete `calculateRefundDeadline(LocalDate start)` method that returns `start.plusDays(7)`.
4. WHEN `markExpiredIfNeeded()` is called and `endDate` is before today and status is ACTIVE, THE AbstractSubscriptionAccount SHALL set status to EXPIRED.
5. THE AbstractSubscriptionAccount SHALL declare `abstract boolean isActive()` to be implemented by each subclass.
6. THE AbstractSubscriptionAccount SHALL declare `abstract void activatePlan(SubscriptionPlan plan)` to enforce subclass-specific activation logic.
7. THE AbstractSubscriptionAccount SHALL declare `abstract void cancel()` to enforce subclass-specific cancellation logic.
8. THE UserAccount_Entity SHALL extend AbstractSubscriptionAccount, implement `isActive()` returning `status == ACTIVE`, and implement `activatePlan()` and `cancel()` with user-specific logic.
9. THE ShopAccount_Entity SHALL extend AbstractSubscriptionAccount, implement `isActive()` returning `status == ACTIVE`, and implement `activatePlan()` and `cancel()` with shop-specific logic.

---

### Requirement 6: AbstractSubscriptionRecord

**User Story:** As a developer, I want UserSubscription and ShopSubscription to share a common record base class, so that refund logic and shared fields are not duplicated.

#### Acceptance Criteria

1. THE AbstractSubscriptionRecord SHALL contain shared fields: `subscriptionId`, `transactionId`, `planId`, `status`, `startDate`, `endDate`, and `refundDeadline`.
2. WHEN `markRefunded()` is called, THE AbstractSubscriptionRecord SHALL set status to REFUNDED.
3. THE UserSubscription_Entity SHALL extend AbstractSubscriptionRecord and remove duplicate shared fields.
4. THE ShopSubscription_Entity SHALL extend AbstractSubscriptionRecord and remove duplicate shared fields.

---

### Requirement 7: AbstractSnapshot

**User Story:** As a developer, I want all analytics snapshot entities to share an immutable base class, so that snapshot identity fields are write-once and consistent.

#### Acceptance Criteria

1. THE AbstractSnapshot SHALL contain immutable fields: `snapshotId` (UUID), `snapshotDate` (LocalDate), `shopId` (UUID), and `createdAt` (LocalDateTime), all set in the constructor.
2. THE AbstractSnapshot SHALL provide no public setters for any of its fields.
3. THE DailyProductSnapshot_Entity, DailyCategorySnapshot_Entity, and DailySnapshot_Entity SHALL each extend AbstractSnapshot and remove duplicate snapshot identity fields.
4. WHEN a snapshot is created, THE AbstractSnapshot SHALL set `createdAt` to `LocalDateTime.now()` and accept no subsequent modification.

---

### Requirement 8: AbstractDomainService

**User Story:** As a developer, I want domain services to share common guard methods for role and null validation, so that inline checks are not duplicated across services.

#### Acceptance Criteria

1. THE AbstractDomainService SHALL provide `protected void requireRole(Role.RoleName actual, Role.RoleName... allowed)` that throws DomainException if `actual` is not in the `allowed` list.
2. THE AbstractDomainService SHALL provide `protected void requireNotNull(Object value, String fieldName)` that throws DomainException if `value` is null.
3. THE NotificationService SHALL extend AbstractDomainService and replace inline role and null checks with `requireRole()` and `requireNotNull()` calls.
4. THE FeedbackService SHALL extend AbstractDomainService and replace inline role and null checks with `requireRole()` and `requireNotNull()` calls.
5. THE ReportingService SHALL extend AbstractDomainService and replace inline role and null checks with `requireRole()` and `requireNotNull()` calls.
6. THE AnalyticsAggregationService SHALL extend AbstractDomainService and replace inline role and null checks with `requireRole()` and `requireNotNull()` calls.
7. WHEN `requireRole()` is called with a role not in the allowed list, THE AbstractDomainService SHALL throw a DomainException with a descriptive message.
8. WHEN `requireNotNull()` is called with a null value, THE AbstractDomainService SHALL throw a DomainException including the field name.

---

### Requirement 9: AbstractAccessPolicy

**User Story:** As a developer, I want all access policy classes to share a common abstract base, so that admin-only enforcement is consistent and domain-specific rules are encapsulated per policy.

#### Acceptance Criteria

1. THE AbstractAccessPolicy SHALL declare `abstract void check(Role.RoleName actorRole, String operation)` to be implemented by each subclass.
2. THE AbstractAccessPolicy SHALL provide a concrete `void requireAdmin(Role.RoleName actorRole)` method that delegates to `check(actorRole, "ADMIN_ONLY")`.
3. THE NotificationAccessPolicy SHALL extend AbstractAccessPolicy and implement `check()` with notification-specific role rules.
4. THE FeedbackAccessPolicy SHALL extend AbstractAccessPolicy and implement `check()` with feedback-specific role rules.
5. THE RolePolicy SHALL extend AbstractAccessPolicy and implement `check()` with inventory role rules.
6. THE AnalyticsAccessPolicy SHALL extend AbstractAccessPolicy and implement `check()` with analytics role rules.
7. WHEN `requireAdmin()` is called with a non-admin role, THE AbstractAccessPolicy SHALL throw a DomainException.

---

### Requirement 10: AbstractSnapshotAggregator

**User Story:** As a developer, I want snapshot aggregation to follow a consistent template-method pattern, so that the idempotency check, aggregation, and persistence steps are always executed in the correct order.

#### Acceptance Criteria

1. THE AbstractSnapshotAggregator SHALL be a generic class `AbstractSnapshotAggregator<T>`.
2. THE AbstractSnapshotAggregator SHALL declare `abstract T aggregate(UUID shopId, LocalDate date)`.
3. THE AbstractSnapshotAggregator SHALL declare `abstract void persist(T snapshot)`.
4. THE AbstractSnapshotAggregator SHALL provide a concrete `boolean alreadyExists(UUID shopId, LocalDate date)` that queries the relevant repository.
5. THE AbstractSnapshotAggregator SHALL provide a template method `void run(UUID shopId, LocalDate date)` that calls `alreadyExists()` first, then `aggregate()`, then `persist()`.
6. WHEN `alreadyExists()` returns true, THE AbstractSnapshotAggregator SHALL skip aggregation and persist without re-running `aggregate()`.

---

### Requirement 11: AbstractReportPeriod

**User Story:** As a developer, I want report period logic to follow a consistent template, so that date range calculation and report building are always coordinated correctly.

#### Acceptance Criteria

1. THE AbstractReportPeriod SHALL declare `abstract DateRange getDateRange(LocalDate endDate)`.
2. THE AbstractReportPeriod SHALL declare `abstract String getPeriodName()`.
3. THE AbstractReportPeriod SHALL provide a concrete `PeriodReportDTO buildReport(UUID shopId, LocalDate endDate, DailySnapshotRepository repo)` that calls `getDateRange()` then queries the repository.

---

### Requirement 12: AbstractDiscountCalculator

**User Story:** As a developer, I want discount calculation to enforce profit margin protection at the base class level, so that no discount strategy can produce a finalPrice below costPrice.

#### Acceptance Criteria

1. THE AbstractDiscountCalculator SHALL declare `abstract BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue)`.
2. THE AbstractDiscountCalculator SHALL provide a concrete `void validateProfitMargin(BigDecimal finalPrice, BigDecimal costPrice)` that throws DomainException if `finalPrice` is less than or equal to `costPrice`.
3. WHEN `validateProfitMargin()` is called with `finalPrice <= costPrice`, THE AbstractDiscountCalculator SHALL throw a DomainException with a profit protection message.

---

### Requirement 13: AbstractNotificationDispatcher

**User Story:** As a developer, I want notification dispatch to follow a consistent template, so that eligibility checks always precede sending and billing alerts are always forced on.

#### Acceptance Criteria

1. THE AbstractNotificationDispatcher SHALL declare `abstract void send(Notification notification)`.
2. THE AbstractNotificationDispatcher SHALL provide a concrete `boolean shouldSend(Notification n, NotificationPreferences prefs)` that returns true for billingAlerts unconditionally and checks preferences for other types.
3. THE AbstractNotificationDispatcher SHALL provide a template method `void dispatch(Notification n, NotificationPreferences prefs)` that calls `shouldSend()` first and only calls `send()` if the result is true.
4. WHEN `shouldSend()` is evaluated for a billingAlerts notification, THE AbstractNotificationDispatcher SHALL always return true regardless of user preferences.

---

### Requirement 14: Repository Interfaces for All Domains

**User Story:** As a developer, I want every domain aggregate to have a repository interface defined in the domain layer, so that the domain depends only on abstractions and infrastructure implementations are interchangeable.

#### Acceptance Criteria

1. THE Auth_Domain SHALL define repository interfaces: `UserRepository`, `UserAuthRepository`, and `MembershipRepository` with save, find, and existence-check methods.
2. THE PlanSubscription_Domain SHALL define repository interfaces: `SubscriptionPlanRepository`, `PlatformRegistryRepository`, `UserAccountRepository`, `ShopAccountRepository`, `UserSubscriptionRepository`, and `ShopSubscriptionRepository`.
3. THE Store_Domain SHALL define a `ShopRepository` interface with save, findById, findBySlug, findByOwnerId, and existsBySlug methods.
4. THE Inventory_Domain SHALL define store interfaces: `ProductStore`, `ProductVariantStore`, `CategoryStore`, `EventTypeStore`, and `EventProductStore`.
5. THE Sale_Domain SHALL define store interfaces: `OrderStore`, `ReceiptStore`, `DeliveryStore`, and `PaymentIntentStore` including `getForUpdate(UUID)`, `existsByBankRefId(String)`, and `delete(UUID)` on PaymentIntentStore.
6. THE Payment_Domain SHALL define repository interfaces: `TransactionRepository`, `InvoiceRepository`, `PaymentMethodRepository`, and `PlatformRevenueSnapshotRepository`.
7. THE Notification_Domain SHALL define repository interfaces: `NotificationRepository`, `NotificationDispatchRepository`, and `NotificationPreferencesRepository`.
8. THE Feedback_Domain SHALL define a `FeatureSuggestionRepository` interface with save, findById, findByUserId, and findByStatus methods.
9. THE Analytics_Domain SHALL define repository interfaces: `DailySnapshotRepository`, `DailyProductSnapshotRepository`, `DailyCategorySnapshotRepository`, `AnalyticsJobLockStore`, `ConfirmedOrderReadRepository`, `InventoryAnalyticsReadRepository`, and `OrderItemReadRepository`.
10. THE Analytics_Domain SHALL define `AnalyticsJobLockStore` with `tryLock(LocalDate)` and `release(LocalDate)` methods to enforce single-job execution per day.
11. IF a repository method is called with an ID that does not exist, THEN THE Repository SHALL return an empty Optional rather than throwing an exception.

---

### Requirement 15: Gateway Interfaces

**User Story:** As a developer, I want external system integrations to be defined as domain-layer interfaces, so that infrastructure implementations can be swapped without touching domain logic.

#### Acceptance Criteria

1. THE EmailGateway_Interface SHALL declare `send(String to, String subject, String body)` in the notification domain package.
2. THE WebhookSecurity_Interface SHALL declare `verifySignature(String payload, String signature)` and `verifyNotReplayed(String nonce)` in the sale-management domain package.
3. THE TransactionRunner_Interface SHALL declare `runInTransaction(Runnable task)` in the sale-management domain package.
4. IF `verifySignature()` fails, THEN THE WebhookSecurity_Interface SHALL throw a DomainException.
5. IF `verifyNotReplayed()` detects a replayed nonce, THEN THE WebhookSecurity_Interface SHALL throw a DomainException.

---

### Requirement 16: Application-Layer Service Interfaces

**User Story:** As a developer, I want every application service to be backed by an interface, so that the application layer depends on abstractions and services are independently testable.

#### Acceptance Criteria

1. THE Application_Layer SHALL define service interfaces: `IAuthService`, `ISubscriptionService`, `IStoreService`, `IInventoryManagementService`, `ISaleOrchestrationService`, `IPaymentService`, `INotificationOrchestrationService`, `IFeedbackOrchestrationService`, `IAnalyticsService`, `IRevenueService`, and `IAdminService`.
2. THE AuthService SHALL implement `IAuthService` and provide: `registerUser`, `registerOAuth`, `login`, `assignMembership`, `revokeMembership`, and `updateUserStatus`.
3. THE SubscriptionService SHALL implement `ISubscriptionService` and provide: `onboardUser`, `activateUserPlan`, `activateShopPlan`, `upgradeUserPlan`, `cancelUserSubscription`, `runExpirationJob`, `userHasFeature`, and `banRegistry`.
4. THE StoreService SHALL implement `IStoreService` and provide: `registerShop`, `verifyShop`, `activateShop`, `suspendShop`, `banShop`, `unbanShop`, `updateAddress`, and `calculatePayout`.
5. THE InventoryManagementService SHALL implement `IInventoryManagementService` and provide: `createProductAtomic`, `updateProduct`, `bulkInsertVariants`, `createEvent`, `attachProductToEvent`, `calculateFinalPrice`, and `createCategory`.
6. THE SaleOrchestrationService SHALL implement `ISaleOrchestrationService` and provide: `createPaymentIntent`, `handlePaymentWebhook`, `finalizeOrder`, `expireStaleIntents`, and `cancelUnpaidOrders`.
7. THE PaymentService SHALL implement `IPaymentService` and provide: `createTransaction`, `markTransactionPaid`, `markTransactionFailed`, `issueInvoice`, `cancelInvoice`, `registerPaymentMethod`, and `generateDailySnapshot`.
8. THE NotificationOrchestrationService SHALL implement `INotificationOrchestrationService` and provide: `sendNotification`, `markRead`, `markAllRead`, `getUnreadCount`, `getNotifications`, `updatePreferences`, and `retryFailedDispatches`.
9. THE FeedbackOrchestrationService SHALL implement `IFeedbackOrchestrationService` and provide: `submitSuggestion`, `editSuggestion`, `updateStatus`, `listMySuggestions`, and `adminListByStatus`.
10. THE AnalyticsService SHALL implement `IAnalyticsService` and provide: `runDailyAggregation`, `getDailyReport`, `getWeeklyReport`, `getMonthlyReport`, `getAnnualReport`, `rankSellers`, `getCategoryTrends`, and `predictOutOfStock`.
11. THE RevenueService SHALL implement `IRevenueService` and provide: `generateDailySnapshot`, `finalizeSnapshot`, `lockSnapshot`, and `getRevenueReport`.

---

### Requirement 17: NotificationSender Polymorphism

**User Story:** As a developer, I want notification dispatch to support multiple channels through a common interface, so that new channels can be added without modifying the dispatch orchestrator.

#### Acceptance Criteria

1. THE NotificationSender_Interface SHALL declare `NotificationChannel getChannel()`, `boolean canSend(Notification notification, NotificationPreferences preferences)`, and `void send(Notification notification)`.
2. THE InAppNotificationSender SHALL implement NotificationSender, return `NotificationChannel.IN_APP` from `getChannel()`, and always return true from `canSend()`.
3. THE EmailNotificationSender SHALL implement NotificationSender, return `NotificationChannel.EMAIL` from `getChannel()`, and return true from `canSend()` only when priority is HIGH and `emailEnabled` is true.
4. THE DispatchService SHALL accept a `List<NotificationSender>` and iterate over all senders, calling `canSend()` before `send()` for each.
5. WHEN a new NotificationSender implementation is added, THE DispatchService SHALL dispatch to it without any code change to the dispatch loop.

---

### Requirement 18: DiscountStrategy Polymorphism

**User Story:** As a developer, I want discount calculation to be pluggable by discount type, so that new discount types can be added without modifying the discount engine.

#### Acceptance Criteria

1. THE DiscountStrategy_Interface SHALL declare `DiscountType getType()`, `BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue)`, and `void validate(BigDecimal discountValue)`.
2. THE PercentageDiscountStrategy SHALL implement DiscountStrategy, apply `basePrice * (1 - discountValue/100)` using BigDecimal with HALF_UP rounding, and validate that `discountValue` is between 0 and 100 inclusive.
3. THE FixedDiscountStrategy SHALL implement DiscountStrategy, apply `basePrice - discountValue`, and validate that `discountValue` is less than or equal to `basePrice`.
4. THE DiscountService SHALL look up the correct DiscountStrategy by `DiscountType` and delegate calculation to it.
5. WHEN `validate()` is called with an out-of-range discount value, THE DiscountStrategy SHALL throw a DomainException.
6. FOR ALL valid inputs, applying a DiscountStrategy then calling `validateProfitMargin()` SHALL produce a finalPrice greater than costPrice.

---

### Requirement 19: SubscriptionAccount Interface and Activation Router

**User Story:** As a developer, I want UserAccount and ShopAccount to be treated uniformly through a common interface, so that the subscription activation router can delegate without knowing the concrete type.

#### Acceptance Criteria

1. THE SubscriptionAccount_Interface SHALL declare: `getSubscriptionId()`, `getPlanId()`, `getRegistryId()`, `activatePlan(SubscriptionPlan plan)`, `expire()`, `cancel()`, `isActive()`, and `markExpiredIfNeeded()`.
2. THE UserAccount_Entity SHALL implement SubscriptionAccount in addition to extending AbstractSubscriptionAccount.
3. THE ShopAccount_Entity SHALL implement SubscriptionAccount in addition to extending AbstractSubscriptionAccount.
4. THE SubscriptionActivationRouter SHALL accept a `TargetType` and return the appropriate `SubscriptionAccount` instance (UserAccount or ShopAccount).
5. WHEN `SubscriptionActivationRouter.route()` is called with an unknown TargetType, THE SubscriptionActivationRouter SHALL throw a DomainException.

---

### Requirement 20: SnapshotAggregator Polymorphism

**User Story:** As a developer, I want snapshot aggregation to be driven by a list of pluggable aggregators, so that new snapshot types can be added without modifying the aggregation orchestrator.

#### Acceptance Criteria

1. THE SnapshotAggregator_Interface SHALL declare: `String getAggregatorName()`, `boolean alreadyExists(UUID shopId, LocalDate date)`, `T aggregate(UUID shopId, LocalDate date)`, and `void persist(T snapshot)`.
2. THE ProductSnapshotAggregator SHALL implement SnapshotAggregator and aggregate from order items and inventory data.
3. THE CategorySnapshotAggregator SHALL implement SnapshotAggregator and aggregate from product snapshots.
4. THE DailySnapshotAggregator SHALL implement SnapshotAggregator and aggregate from category snapshots.
5. THE AnalyticsAggregationService SHALL iterate aggregators in dependency order: ProductSnapshotAggregator → CategorySnapshotAggregator → DailySnapshotAggregator.
6. WHEN `alreadyExists()` returns true for a given shopId and date, THE SnapshotAggregator SHALL skip that aggregation without error (idempotent).

---

### Requirement 21: ReportPeriodStrategy Polymorphism

**User Story:** As a developer, I want report period date ranges to be determined by pluggable strategies, so that daily, weekly, monthly, and annual reports share the same query infrastructure.

#### Acceptance Criteria

1. THE ReportPeriodStrategy_Interface SHALL declare: `DateRange getDateRange(LocalDate endDate)`, `String getPeriodName()`, and `boolean requiresOwnerRole()`.
2. THE DailyReportStrategy SHALL implement ReportPeriodStrategy and return a single-day range.
3. THE WeeklyReportStrategy SHALL implement ReportPeriodStrategy and return a range from `endDate - 6 days` to `endDate`.
4. THE MonthlyReportStrategy SHALL implement ReportPeriodStrategy and return a range from the first day of the month to `endDate`.
5. THE AnnualReportStrategy SHALL implement ReportPeriodStrategy, return a range from the first day of the year to `endDate`, and return true from `requiresOwnerRole()`.
6. THE ReportingService SHALL accept a ReportPeriodStrategy and use `getDateRange()` to determine the query window.

---

### Requirement 22: PaymentProcessor Polymorphism

**User Story:** As a developer, I want payment processing to be pluggable by payment method type, so that card and QR code flows are handled by separate processors without branching in the payment service.

#### Acceptance Criteria

1. THE PaymentProcessor_Interface SHALL declare: `CardType getSupportedCardType()`, `PaymentIntent createIntent(Transaction transaction)`, and `boolean verify(String gatewayRef, BigDecimal amount)`.
2. THE CardPaymentProcessor SHALL implement PaymentProcessor and handle VISA, MASTERCARD, and AMEX card types.
3. THE QrCodePaymentProcessor SHALL implement PaymentProcessor, return null from `getSupportedCardType()`, and handle QR code generation and verification.
4. THE PaymentService SHALL look up the correct PaymentProcessor by payment method type and delegate intent creation and verification to it.
5. WHEN `verify()` returns false, THE PaymentProcessor SHALL cause the PaymentService to throw a DomainException.

---

### Requirement 23: ForecastStrategy Polymorphism

**User Story:** As a developer, I want out-of-stock prediction to be driven by a configurable forecast strategy, so that the algorithm can be swapped without modifying the forecast service.

#### Acceptance Criteria

1. THE ForecastStrategy_Interface SHALL declare: `String getForecastType()` and `List<OutOfStockPredictionDTO> predict(List<DailyProductSnapshot> snapshots)`.
2. THE LinearTrendForecastStrategy SHALL implement ForecastStrategy using simple linear regression on `qtySold` values.
3. THE MovingAverageForecastStrategy SHALL implement ForecastStrategy using a 7-day moving average on `qtySold` values.
4. THE ForecastService SHALL accept a configurable ForecastStrategy and delegate prediction to it.
5. WHEN the snapshot list is empty, THE ForecastStrategy SHALL return an empty list without throwing an exception.

---

### Requirement 24: AccessPolicy Polymorphism

**User Story:** As a developer, I want all access policy classes to implement a common interface, so that policies can be injected and invoked uniformly across domain services.

#### Acceptance Criteria

1. THE AccessPolicy_Interface SHALL declare `void check(Role.RoleName actorRole, String operation)`.
2. THE AnalyticsAccessPolicy SHALL implement AccessPolicy and enforce analytics-specific role rules.
3. THE FeedbackAccessPolicy SHALL implement AccessPolicy and enforce feedback-specific role rules.
4. THE NotificationAccessPolicy SHALL implement AccessPolicy and enforce notification-specific role rules.
5. THE InventoryRolePolicy SHALL implement AccessPolicy and enforce inventory-specific role rules.
6. WHEN `check()` is called with an unauthorized role, THE AccessPolicy SHALL throw a DomainException.
7. FOR ALL AccessPolicy implementations, calling `check()` with a SUPER_ADMIN role SHALL succeed for any operation.

---

### Requirement 25: Platform-Wide Invariants and Constraints

**User Story:** As a developer, I want all OOP constructs to enforce the platform's core invariants, so that business rules cannot be violated through the new class hierarchy.

#### Acceptance Criteria

1. THE Velora_Platform SHALL represent all monetary values using BigDecimal with scale=2 and HALF_UP rounding throughout all abstract classes, interfaces, and implementations.
2. THE Velora_Platform SHALL use UUID for all entity identifiers across all abstract base classes and interfaces.
3. THE Velora_Platform SHALL enforce no-default-constructor policy — all entities and abstract classes SHALL require mandatory fields via constructor parameters.
4. WHILE a PlatformRegistry status is BANNED, THE SubscriptionService SHALL reject any activation attempt without an admin override flag.
5. WHEN `validateProfitMargin()` is called, THE AbstractDiscountCalculator SHALL throw a DomainException if `finalPrice` is not strictly greater than `costPrice`.
6. THE AbstractSnapshot SHALL enforce write-once semantics — no field may be modified after construction.
7. WHEN `shouldSend()` is evaluated for a billingAlerts notification type, THE AbstractNotificationDispatcher SHALL return true regardless of user preference settings.
8. THE Velora_Platform SHALL scope all repository queries by `shopId` where applicable to enforce row-level access security.
9. IF a domain rule is violated at any layer, THEN THE Velora_Platform SHALL throw a DomainException with a descriptive message identifying the violated rule.
10. FOR ALL parsers and serializers introduced during this OOP implementation, THE Velora_Platform SHALL support a round-trip property: parse(format(x)) produces an equivalent object.

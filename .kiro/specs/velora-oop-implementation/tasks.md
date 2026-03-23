# Implementation Plan: Velora OOP Implementation

## Overview

Apply the four OOP pillars — Inheritance, Abstraction, Interfaces, and Polymorphism — across all 11 domain modules of the Velora platform. Tasks are ordered by OOP concept and build incrementally so each step integrates into the previous.

## Tasks

---

## PHASE 1 — INHERITANCE

> Goal: Eliminate duplicate fields across entities by introducing shared abstract base classes.
> Demonstrates: code reuse through class hierarchy, `extends` keyword, `super()` constructor chaining.

- [x] INH-01 — Create `AbstractEntity`
  - [x] INH-01.1 Create `src/main/java/com/velora/app/common/AbstractEntity.java`
    - Field: `UUID id` (set in constructor, immutable, no public setter)
    - Override `equals(Object)` and `hashCode()` based solely on `id`
    - Override `toString()` returning `ClassName{id=<uuid>}`
    - Throw `DomainException` if null `id` is passed to constructor
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  - [ ]* INH-01.2 Write property tests for `AbstractEntity` — `AbstractEntityTest`
    - **Property 1: UUID Identity Equality** — two instances with same UUID must be equal; different UUIDs must not be equal
    - **Property 2: toString Format** — `toString()` must match pattern `ClassName{id=<uuid>}`
    - **Validates: Requirements 1.2, 1.3, 1.4**
  - [ ]* INH-01.3 Write unit test for `AbstractEntity` — null id constructor throws `DomainException`
    - **Validates: Requirement 1.5**

- [x] INH-02 — Create `AbstractAuditableEntity`
  - [x] INH-02.1 Create `src/main/java/com/velora/app/common/AbstractAuditableEntity.java`
    - Extends `AbstractEntity`
    - Field: `LocalDateTime createdAt` (set in constructor, immutable)
    - Field: `LocalDateTime updatedAt` (mutable, managed via `touch()` only)
    - Method: `protected void touch()` — sets `updatedAt = LocalDateTime.now()`
    - Expose `createdAt` and `updatedAt` as read-only via getters
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  - [ ]* INH-02.2 Write property test for `AbstractAuditableEntity` — `AbstractAuditableEntityTest`
    - **Property 3: touch() Updates updatedAt** — calling `touch()` must result in `updatedAt >= previous updatedAt`
    - **Validates: Requirements 2.3, 2.4, 4.7**

- [x] INH-03 — Apply Inheritance to Auth Domain
  - [x] INH-03.1 Refactor `src/main/java/com/velora/app/core/domain/auth/User.java`
    - `User extends AbstractAuditableEntity` — remove duplicate `id`, `createdAt`, `updatedAt`
    - _Requirements: 3.1_
  - [x] INH-03.2 Refactor `src/main/java/com/velora/app/core/domain/auth/UserAuth.java`
    - `UserAuth extends AbstractAuditableEntity` — remove duplicate `createdAt`
    - _Requirements: 3.2_
  - [x] INH-03.3 Refactor `src/main/java/com/velora/app/core/domain/auth/Membership.java`
    - `Membership extends AbstractAuditableEntity` — remove duplicate timestamps, call `touch()` on any mutation method
    - _Requirements: 3.3, 3.5_
  - [x] INH-03.4 Refactor `src/main/java/com/velora/app/core/domain/auth/Role.java`
    - `Role extends AbstractEntity` — remove duplicate `roleId`, `equals()`, `hashCode()`
    - _Requirements: 3.4_

- [x] INH-04 — Apply Inheritance to Store Domain
  - [x] INH-04.1 Refactor `src/main/java/com/velora/app/core/domain/store-management/Shop.java`
    - `Shop extends AbstractAuditableEntity` — remove duplicate `id`, `createdAt`, `updatedAt`
    - Call `touch()` inside `transitionStatus()` and `updateAddress()`
    - _Requirements: 4.1_

- [x] INH-05 — Apply Inheritance to Inventory Domain
  - [x] INH-05.1 Refactor `Product.java`, `ProductVariant.java`, `Category.java`, `EventType.java`, `EventProduct.java` in `inventory-event-menagement`
    - All five extend `AbstractAuditableEntity`
    - Remove duplicate `id`, `createdAt`, `updatedAt` from each
    - Call `touch()` on any mutation method
    - _Requirements: 4.2, 4.7_

- [x] INH-06 — Apply Inheritance to Sale Domain
  - [x] INH-06.1 Refactor `Order.java`, `OrderItem.java`, `Receipt.java`, `Delivery.java`, `PaymentIntent.java` in `sale-management`
    - All five extend `AbstractAuditableEntity`
    - Remove duplicate `id`, `createdAt`, `updatedAt` from each
    - _Requirements: 4.3_

- [x] INH-07 — Apply Inheritance to Payment Domain
  - [x] INH-07.1 Refactor `Transaction.java`, `Invoice.java`, `PaymentMethod.java` in `payment`
    - All three extend `AbstractAuditableEntity`
    - Remove duplicate `id`, `createdAt`, `updatedAt` from each
    - _Requirements: 4.4_

- [x] INH-08 — Apply Inheritance to Notification Domain
  - [x] INH-08.1 Refactor `Notification.java`, `NotificationPreferences.java`, `NotificationDispatchRecord.java` in `notification`
    - All three extend `AbstractAuditableEntity`
    - Remove duplicate `id`, `createdAt`, `updatedAt` from each
    - _Requirements: 4.5_

- [x] INH-09 — Apply Inheritance to Feedback Domain
  - [x] INH-09.1 Refactor `src/main/java/com/velora/app/core/domain/feedback/FeatureSuggestion.java`
    - `FeatureSuggestion extends AbstractAuditableEntity`
    - Remove duplicate `id`, `createdAt`, `updatedAt`
    - Call `touch()` inside `edit()` and `updateStatus()`
    - _Requirements: 4.6, 4.7_

- [x] INH-10 — Create `AbstractSubscriptionAccount`
  - [x] INH-10.1 Create `src/main/java/com/velora/app/common/AbstractSubscriptionAccount.java`
    - Fields: `subscriptionId`, `planId`, `registryId`, `status`, `startDate`, `endDate`, `refundDeadline`, `currentPlanDurationMonths`
    - Method: `calculateEndDate(LocalDate start, int months)` — returns `start.plusMonths(months)`
    - Method: `calculateRefundDeadline(LocalDate start)` — returns `start.plusDays(14)`
    - Method: `markExpiredIfNeeded()` — if `endDate.isBefore(today)` and status is ACTIVE, set EXPIRED
    - Abstract methods: `boolean isActive()`, `void activatePlan(SubscriptionPlan plan)`, `void cancel()`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_
  - [ ]* INH-10.2 Write property tests for `AbstractSubscriptionAccount` — `AbstractSubscriptionAccountTest`
    - **Property 4: calculateEndDate Correctness** — `calculateEndDate(start, months)` must equal `start.plusMonths(months)` for any positive months
    - **Property 5: calculateRefundDeadline is 14 Days** — `calculateRefundDeadline(start)` must equal `start.plusDays(14)` for any start date
    - **Property 6: markExpiredIfNeeded Transitions to EXPIRED** — when `endDate` is before today and status is ACTIVE, must transition to EXPIRED
    - **Property 7: isActive Reflects ACTIVE Status** — `isActive()` returns true iff `status == ACTIVE`
    - **Validates: Requirements 5.2, 5.3, 5.4, 5.8, 5.9**

- [x] INH-11 — Apply `AbstractSubscriptionAccount` to Subscription Entities
  - [x] INH-11.1 Refactor `UserAccount.java` and `ShopAccount.java` in `plan_subscription`
    - Both extend `AbstractSubscriptionAccount`
    - Remove duplicate shared fields
    - Keep domain-specific methods: `activatePlan()`, `upgrade()`, `renew()`, `cancel()`, `expire()`
    - Implement `isActive()` — return `status == ACTIVE`
    - _Requirements: 5.8, 5.9_

- [x] INH-12 — Create `AbstractSubscriptionRecord`
  - [x] INH-12.1 Create `src/main/java/com/velora/app/common/AbstractSubscriptionRecord.java`
    - Fields: `subscriptionId`, `transactionId`, `planId`, `status`, `startDate`, `endDate`, `refundDeadline`
    - Method: `markRefunded()` — sets status to REFUNDED
    - _Requirements: 6.1, 6.2_
  - [ ]* INH-12.2 Write unit test for `AbstractSubscriptionRecord` — `markRefunded()` sets status to REFUNDED
    - **Validates: Requirement 6.2**

- [x] INH-13 — Apply `AbstractSubscriptionRecord` to Subscription Records
  - [x] INH-13.1 Refactor `UserSubscription.java` and `ShopSubscription.java` in `plan_subscription`
    - Both extend `AbstractSubscriptionRecord`
    - Remove duplicate shared fields
    - _Requirements: 6.3, 6.4_

- [x] INH-14 — Create `AbstractSnapshot`
  - [x] INH-14.1 Create `src/main/java/com/velora/app/common/AbstractSnapshot.java`
    - Fields (all immutable, set in constructor): `snapshotId` (UUID), `snapshotDate` (LocalDate), `shopId` (UUID), `createdAt` (LocalDateTime)
    - No public setters — enforce write-once semantics
    - _Requirements: 7.1, 7.2, 7.4, 25.6_
  - [ ]* INH-14.2 Write property test for `AbstractSnapshot` — `AbstractSnapshotTest`
    - **Property 8: AbstractSnapshot Write-Once** — `snapshotId`, `snapshotDate`, `shopId`, `createdAt` must be identical before and after any non-reconstructing operation
    - **Validates: Requirements 7.1, 7.4, 25.6**

- [x] INH-15 — Apply `AbstractSnapshot` to Analytics Domain
  - [x] INH-15.1 Refactor `DailyProductSnapshot.java`, `DailyCategorySnapshot.java`, `DailySnapshot.java` in `report-and-analytic`
    - All three extend `AbstractSnapshot`
    - Remove duplicate `snapshotId`, `snapshotDate`, `shopId`, `createdAt`
    - _Requirements: 7.3_

- [x] INH-16 — Create `AbstractDomainService`
  - [x] INH-16.1 Create `src/main/java/com/velora/app/common/AbstractDomainService.java`
    - Method: `protected void requireRole(Role.RoleName actual, Role.RoleName... allowed)` — throws `DomainException` if not in allowed list
    - Method: `protected void requireNotNull(Object value, String fieldName)` — throws `DomainException` if null, message includes fieldName
    - _Requirements: 8.1, 8.2, 8.7, 8.8_
  - [ ]* INH-16.2 Write property tests for `AbstractDomainService` — `AbstractDomainServiceTest`
    - **Property 9: requireRole Throws for Unauthorized Roles** — any role not in allowed list must throw `DomainException`
    - **Property 10: requireNotNull Throws for Null Values** — any null value must throw `DomainException` whose message contains fieldName
    - **Validates: Requirements 8.1, 8.2, 8.7, 8.8**

- [x] INH-17 — Apply `AbstractDomainService` to Domain Services
  - [x] INH-17.1 Refactor `NotificationService.java`, `FeedbackService.java`, `ReportingService.java`, `AnalyticsAggregationService.java`
    - All four extend `AbstractDomainService`
    - Replace inline role checks with `requireRole(...)` calls
    - Replace inline null checks with `requireNotNull(...)` calls
    - _Requirements: 8.3, 8.4, 8.5, 8.6_

- [x] Checkpoint — Phase 1 complete
  - Ensure all tests pass, ask the user if questions arise.

---

## PHASE 2 — ABSTRACTION

> Goal: Hide implementation details behind abstract classes with abstract methods. Force subclasses to provide domain-specific behavior while sharing common logic.
> Demonstrates: `abstract class`, `abstract` methods, template method pattern.

- [x] ABS-01 — Abstract `AbstractAccessPolicy`
  - [x] ABS-01.1 Create `src/main/java/com/velora/app/common/AbstractAccessPolicy.java`
    - Abstract method: `abstract void check(Role.RoleName actorRole, String operation)`
    - Concrete method: `void requireAdmin(Role.RoleName actorRole)` — delegates to `check(actorRole, "ADMIN_ONLY")`
    - _Requirements: 9.1, 9.2_
  - [ ]* ABS-01.2 Write unit test for `AbstractAccessPolicy` — `requireAdmin()` with non-admin role throws `DomainException`
    - **Validates: Requirement 9.7**

- [x] ABS-02 — Apply `AbstractAccessPolicy` to Domain Policies
  - [x] ABS-02.1 Refactor `NotificationAccessPolicy.java`, `FeedbackAccessPolicy.java`, `RolePolicy.java`, `AnalyticsAccessPolicy.java`
    - All four extend `AbstractAccessPolicy`
    - Each implements `check()` with domain-specific role rules
    - Use inherited `requireAdmin()` where applicable
    - _Requirements: 9.3, 9.4, 9.5, 9.6_

- [x] ABS-03 — Abstract `AbstractSubscriptionAccount` Lifecycle Methods
  - [x] ABS-03.1 Update `AbstractSubscriptionAccount.java` and refactor `UserAccount.java`, `ShopAccount.java`
    - `activatePlan(SubscriptionPlan plan)` and `cancel()` are declared abstract in `AbstractSubscriptionAccount`
    - `UserAccount.activatePlan()` — sets user-specific fields, calls `calculateEndDate()`, sets `refundDeadline` via `calculateRefundDeadline()`
    - `ShopAccount.activatePlan()` — same pattern with shop-specific fields and `autoRenew` handling
    - Both implement `cancel()` with their own cancellation state transitions
    - _Requirements: 5.6, 5.7, 5.8, 5.9_

- [x] ABS-04 — Abstract `AbstractSnapshotAggregator`
  - [x] ABS-04.1 Create `src/main/java/com/velora/app/common/AbstractSnapshotAggregator.java`
    - Generic class: `AbstractSnapshotAggregator<T>`
    - Abstract methods: `abstract T aggregate(UUID shopId, LocalDate date)`, `abstract void persist(T snapshot)`
    - Concrete method: `boolean alreadyExists(UUID shopId, LocalDate date)` — queries repository
    - Template method: `final void run(UUID shopId, LocalDate date)` — calls `alreadyExists()` → `aggregate()` → `persist()`; skips if already exists
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_
  - [ ]* ABS-04.2 Write property test for `AbstractSnapshotAggregator` — `AbstractSnapshotAggregatorTest`
    - **Property 11: AbstractSnapshotAggregator Idempotency** — when `alreadyExists()` returns true, `run()` must not invoke `aggregate()` or `persist()`
    - **Validates: Requirements 10.5, 10.6, 20.6**

- [x] ABS-05 — Abstract `AbstractReportPeriod`
  - [x] ABS-05.1 Create `src/main/java/com/velora/app/common/AbstractReportPeriod.java`
    - Abstract methods: `abstract DateRange getDateRange(LocalDate endDate)`, `abstract String getPeriodName()`
    - Concrete method: `PeriodReportDTO buildReport(UUID shopId, LocalDate endDate, DailySnapshotRepository repo)` — calls `getDateRange()` then queries repo
    - _Requirements: 11.1, 11.2, 11.3_
  - [ ]* ABS-05.2 Write property test for `AbstractReportPeriod` — `AbstractReportPeriodTest`
    - **Property 12: AbstractReportPeriod Date Range Coverage** — `buildReport()` must query the repository using exactly the `DateRange` returned by `getDateRange(endDate)`
    - **Validates: Requirement 11.3**

- [x] ABS-06 — Abstract `AbstractDiscountCalculator`
  - [x] ABS-06.1 Create `src/main/java/com/velora/app/common/AbstractDiscountCalculator.java`
    - Abstract method: `abstract BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue)`
    - Concrete method: `void validateProfitMargin(BigDecimal finalPrice, BigDecimal costPrice)` — throws `DomainException` if `finalPrice <= costPrice`
    - _Requirements: 12.1, 12.2, 12.3, 25.5_
  - [ ]* ABS-06.2 Write property test for `AbstractDiscountCalculator` — `AbstractDiscountCalculatorTest`
    - **Property 13: validateProfitMargin Throws When finalPrice <= costPrice** — any `finalPrice.compareTo(costPrice) <= 0` must throw `DomainException`
    - **Validates: Requirements 12.2, 12.3, 25.5**

- [x] ABS-07 — Abstract `AbstractNotificationDispatcher`
  - [x] ABS-07.1 Create `src/main/java/com/velora/app/common/AbstractNotificationDispatcher.java`
    - Abstract method: `abstract void send(Notification notification)`
    - Concrete method: `boolean shouldSend(Notification n, NotificationPreferences prefs)` — billingAlerts always true, others check prefs
    - Template method: `final void dispatch(Notification n, NotificationPreferences prefs)` — calls `shouldSend()` → `send()` if true
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 25.7_
  - [ ]* ABS-07.2 Write property test for `AbstractNotificationDispatcher` — `AbstractNotificationDispatcherTest`
    - **Property 14: billingAlerts Always Dispatched** — for any `BILLING_ALERT` notification and any preferences, `shouldSend()` must return true
    - **Validates: Requirements 13.2, 13.4, 25.7**

- [x] Checkpoint — Phase 2 complete
  - Ensure all tests pass, ask the user if questions arise.

---

## PHASE 3 — INTERFACE

> Goal: Define contracts for all repositories, gateways, and services. Domain depends only on interfaces, not implementations.
> Demonstrates: `interface` keyword, dependency inversion, loose coupling.

- [x] INT-01 — Auth Repository Interfaces
  - [x] INT-01.1 Create `UserRepository.java`, `UserAuthRepository.java`, `MembershipRepository.java` in `com.velora.app.core.domain.auth`
    - `UserRepository` — `save(User)`, `findById(UUID)`, `findByUsername(String)`, `existsByUsername(String)`
    - `UserAuthRepository` — `save(UserAuth)`, `findByEmail(String)`, `findByUserId(UUID)`, `existsByEmail(String)`
    - `MembershipRepository` — `save(Membership)`, `findByUserId(UUID)`, `findByShopId(UUID)`, `findByUserAndShop(UUID, UUID)`
    - All `findById` and lookup methods return `Optional<T>`
    - _Requirements: 14.1, 14.11_
  - [ ]* INT-01.2 Write property test for Auth repositories — `PostgresRepositoryIntegrationTest`
    - **Property 15: Repository Returns Empty Optional for Missing IDs** — `findById(id)` for non-existent id must return `Optional.empty()`
    - **Validates: Requirement 14.11**

- [x] INT-02 — Subscription Repository Interfaces
  - [x] INT-02.1 Create repository interfaces in `com.velora.app.core.domain.plan_subscription`
    - `SubscriptionPlanRepository` — `save`, `findById`, `findBySlug`, `findAllActive`
    - `PlatformRegistryRepository` — `save`, `findById`, `findByOwnerId`
    - `UserAccountRepository` — `save`, `findByUserId`, `findAllActive`
    - `ShopAccountRepository` — `save`, `findByShopId`, `findAllActive`
    - `UserSubscriptionRepository` — `save`, `findByUserId`
    - `ShopSubscriptionRepository` — `save`, `findByShopId`
    - _Requirements: 14.2_

- [x] INT-03 — Store Repository Interface
  - [x] INT-03.1 Verify/create `ShopRepository.java` in `com.velora.app.core.domain.store-management`
    - `save(Shop)`, `findById(UUID)`, `findBySlug(String)`, `findByOwnerId(UUID)`, `existsBySlug(String)`
    - _Requirements: 14.3_

- [x] INT-04 — Inventory Store Interfaces
  - [x] INT-04.1 Verify `ProductStore.java`, `ProductVariantStore.java`, `CategoryStore.java`, `EventTypeStore.java`, `EventProductStore.java` in `inventory-event-menagement`
    - `ProductStore` — `save(Product)`, `findById(UUID)`, `findByShopId(UUID)`
    - `ProductVariantStore` — `save(ProductVariant)`, `findByProductId(UUID)`, `findBySku(String)`
    - `CategoryStore` — `save(Category)`, `findByShopId(UUID)`
    - `EventTypeStore` — `save(EventType)`, `findByShopId(UUID)`
    - `EventProductStore` — `save(EventProduct)`, `findByEventId(UUID)`, `findByProductId(UUID)`
    - _Requirements: 14.4_

- [x] INT-05 — Sale Store Interfaces
  - [x] INT-05.1 Verify `OrderStore.java`, `ReceiptStore.java`, `DeliveryStore.java`, `PaymentIntentStore.java` in `sale-management`
    - `OrderStore` — `save(Order)`, `findById(UUID)`, `findByShopId(UUID)`
    - `ReceiptStore` — `save(Receipt)`, `findByOrderId(UUID)`
    - `DeliveryStore` — `save(Delivery)`, `findByOrderId(UUID)`
    - `PaymentIntentStore` — `save(PaymentIntent)`, `findByBankRefId(String)`, `getForUpdate(UUID)`, `existsByBankRefId(String)`, `delete(UUID)`
    - _Requirements: 14.5_

- [x] INT-06 — Payment Repository Interfaces
  - [x] INT-06.1 Create `TransactionRepository.java`, `InvoiceRepository.java`, `PaymentMethodRepository.java`, `PlatformRevenueSnapshotRepository.java` in `com.velora.app.core.domain.payment`
    - `TransactionRepository` — `save(Transaction)`, `findById(UUID)`, `findByGatewayRef(String)`
    - `InvoiceRepository` — `save(Invoice)`, `findById(UUID)`, `findByTransactionId(UUID)`
    - `PaymentMethodRepository` — `save(PaymentMethod)`, `findById(UUID)`
    - `PlatformRevenueSnapshotRepository` — `save(PlatformRevenueSnapshot)`, `findByDate(LocalDate)`
    - _Requirements: 14.6_

- [x] INT-07 — Notification Repository Interfaces
  - [x] INT-07.1 Verify `NotificationRepository.java`, `NotificationDispatchRepository.java`, `NotificationPreferencesRepository.java` in `notification`
    - `NotificationRepository` — `append`, `markRead`, `markAllRead`, `countUnread`, `findUserNotifications`
    - `NotificationDispatchRepository` — `createIfAbsent`, `findPending`, `save`
    - `NotificationPreferencesRepository` — `save`, `findByUserId`
    - _Requirements: 14.7_

- [x] INT-08 — Feedback Repository Interface
  - [x] INT-08.1 Verify `FeatureSuggestionRepository.java` in `feedback`
    - `save(FeatureSuggestion)`, `findById(UUID)`, `findByUserId(UUID)`, `findByStatus(SuggestionStatus)`
    - _Requirements: 14.8_

- [x] INT-09 — Analytics Repository Interfaces
  - [x] INT-09.1 Verify/create analytics repository interfaces in `report-and-analytic`
    - `DailySnapshotRepository` — `save`, `findByShopAndDate`, `findByShopAndDateRange`
    - `DailyProductSnapshotRepository` — `save`, `findByShopAndDate`
    - `DailyCategorySnapshotRepository` — `save`, `findByShopAndDate`
    - `AnalyticsJobLockStore` — `tryLock(LocalDate)`, `release(LocalDate)`
    - `ConfirmedOrderReadRepository` — `findConfirmedOrdersByDate(LocalDate)`
    - `InventoryAnalyticsReadRepository` — `findStockAtMidnight(LocalDate)`
    - `OrderItemReadRepository` — `findOrderItemsByDate(LocalDate)`
    - _Requirements: 14.9, 14.10_

- [x] INT-10 — Gateway Interfaces
  - [x] INT-10.1 Verify/create `EmailGateway.java` in `notification`, `WebhookSecurity.java` and `TransactionRunner.java` in `sale-management`
    - `EmailGateway` — `send(String to, String subject, String body)`
    - `WebhookSecurity` — `verifySignature(String payload, String signature)`, `verifyNotReplayed(String nonce)` — both throw `DomainException` on failure
    - `TransactionRunner` — `runInTransaction(Runnable task)`
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_
  - [ ]* INT-10.2 Write unit test for `WebhookSecurity` — `verifySignature()` failure throws `DomainException`
    - **Validates: Requirement 15.4**

- [ ] INT-11 — Service Interfaces (Application Layer)
  - [ ] INT-11.1 Create service interfaces in `com.velora.app.core.service`
    - `IAuthService`, `ISubscriptionService`, `IStoreService`, `IInventoryManagementService`
    - `ISaleOrchestrationService`, `IPaymentService`, `INotificationOrchestrationService`
    - `IFeedbackOrchestrationService`, `IAnalyticsService`, `IRevenueService`, `IAdminService`
    - Each interface declares all public methods from the corresponding service
    - _Requirements: 16.1_

- [ ] INT-12 — Implement `AuthService`
  - [ ] INT-12.1 Create `src/main/java/com/velora/app/core/service/auth/AuthService.java`
    - `AuthService implements IAuthService`
    - `registerUser(username, email, rawPassword)` — hash password, validate uniqueness, create User + UserAuth
    - `registerOAuth(username, email, provider, providerUid)` — create User + UserAuth with OAuth provider
    - `login(email, rawPassword)` — find UserAuth, verify password, return UserAuth
    - `assignMembership(actorId, userId, shopId, role)` — role check OWNER/SUPER_ADMIN, create Membership
    - `revokeMembership(actorId, membershipId)` — role check, delete Membership
    - `updateUserStatus(actorId, userId, newStatus)` — admin only, update User status
    - _Requirements: 16.2_

- [ ] INT-13 — Implement `SubscriptionService`
  - [ ] INT-13.1 Create `src/main/java/com/velora/app/core/service/subscription/SubscriptionService.java`
    - `SubscriptionService implements ISubscriptionService`
    - `onboardUser(userId, basicPlanId)` — create PlatformRegistry, create UserAccount, activate free plan
    - `activateUserPlan(userId, planId, transactionId)` — load UserAccount, call `activatePlan(plan)`
    - `activateShopPlan(shopId, planId, transactionId)` — load ShopAccount, call `activatePlan(plan)`
    - `upgradeUserPlan(userId, newPlanId)` — load UserAccount, call `upgrade(plan)`
    - `cancelUserSubscription(userId)` — load UserAccount, call `cancel()`
    - `runExpirationJob()` — load all ACTIVE accounts, call `markExpiredIfNeeded()` on each
    - `userHasFeature(userId, featureKey)` — load UserAccount plan, call `hasFeature(featureKey)`
    - `banRegistry(actorId, registryId, reason)` — admin only, call `PlatformRegistry.ban(reason)`
    - _Requirements: 16.3_

- [ ] INT-14 — Implement `StoreService`
  - [ ] INT-14.1 Create `src/main/java/com/velora/app/core/service/store/StoreService.java`
    - `StoreService implements IStoreService`
    - `registerShop`, `verifyShop`, `activateShop`, `suspendShop`, `banShop`, `unbanShop`, `updateAddress`, `calculatePayout`
    - _Requirements: 16.4_

- [ ] INT-15 — Implement `InventoryManagementService`
  - [ ] INT-15.1 Create `src/main/java/com/velora/app/core/service/inventory/InventoryManagementService.java`
    - `InventoryManagementService implements IInventoryManagementService`
    - `createProductAtomic`, `updateProduct`, `bulkInsertVariants`, `createEvent`, `attachProductToEvent`, `calculateFinalPrice`, `createCategory`
    - _Requirements: 16.5_

- [ ] INT-16 — Implement `SaleOrchestrationService`
  - [ ] INT-16.1 Create `src/main/java/com/velora/app/core/service/sale/SaleOrchestrationService.java`
    - `SaleOrchestrationService implements ISaleOrchestrationService`
    - `createPaymentIntent`, `handlePaymentWebhook`, `finalizeOrder`, `expireStaleIntents`, `cancelUnpaidOrders`
    - _Requirements: 16.6_

- [ ] INT-17 — Implement `PaymentService`
  - [ ] INT-17.1 Create `src/main/java/com/velora/app/core/service/payment/PaymentService.java`
    - `PaymentService implements IPaymentService`
    - `createTransaction`, `markTransactionPaid`, `markTransactionFailed`, `issueInvoice`, `cancelInvoice`, `registerPaymentMethod`, `generateDailySnapshot`
    - _Requirements: 16.7_

- [ ] INT-18 — Implement `NotificationOrchestrationService`
  - [ ] INT-18.1 Create `src/main/java/com/velora/app/core/service/notification/NotificationOrchestrationService.java`
    - `NotificationOrchestrationService implements INotificationOrchestrationService`
    - `sendNotification`, `markRead`, `markAllRead`, `getUnreadCount`, `getNotifications`, `updatePreferences`, `retryFailedDispatches`
    - _Requirements: 16.8_

- [ ] INT-19 — Implement `FeedbackOrchestrationService`
  - [ ] INT-19.1 Create `src/main/java/com/velora/app/core/service/feedback/FeedbackOrchestrationService.java`
    - `FeedbackOrchestrationService implements IFeedbackOrchestrationService`
    - `submitSuggestion`, `editSuggestion`, `updateStatus`, `listMySuggestions`, `adminListByStatus`
    - _Requirements: 16.9_

- [ ] INT-20 — Implement `AnalyticsService`
  - [ ] INT-20.1 Create `src/main/java/com/velora/app/core/service/analytics/AnalyticsService.java`
    - `AnalyticsService implements IAnalyticsService`
    - `runDailyAggregation`, `getDailyReport`, `getWeeklyReport`, `getMonthlyReport`, `getAnnualReport`, `rankSellers`, `getCategoryTrends`, `predictOutOfStock`
    - _Requirements: 16.10_

- [ ] INT-21 — Implement `RevenueService`
  - [ ] INT-21.1 Create `src/main/java/com/velora/app/core/service/revenue/RevenueService.java`
    - `RevenueService implements IRevenueService`
    - `generateDailySnapshot`, `getRangeSummary`, `getYearlyReport`, `finalizeSnapshot`, `lockSnapshot`
    - _Requirements: 16.11_

- [ ] INT-22 — Implement `AdminService`
  - [ ] INT-22.1 Create `src/main/java/com/velora/app/core/service/admin/AdminService.java`
    - `AdminService implements IAdminService`
    - `banUser`, `revokeUserSessions`, `banShop`, `unbanShop`, `viewRevenueSnapshots`, `changePermissions`
    - _Requirements: 16.1_

- [ ] INT-23 — Implement Infrastructure Repositories (PostgreSQL)
  - [ ] INT-23.1 Create `Postgres*` implementations in `com.velora.app.infrastructure.db`
    - `PostgresUserRepository implements UserRepository`
    - `PostgresUserAuthRepository implements UserAuthRepository`
    - `PostgresMembershipRepository implements MembershipRepository`
    - `PostgresSubscriptionPlanRepository implements SubscriptionPlanRepository`
    - `PostgresPlatformRegistryRepository implements PlatformRegistryRepository`
    - `PostgresUserAccountRepository implements UserAccountRepository`
    - `PostgresShopAccountRepository implements ShopAccountRepository`
    - `PostgresShopRepository implements ShopRepository`
    - `PostgresProductStore implements ProductStore`
    - `PostgresTransactionRepository implements TransactionRepository`
    - `PostgresInvoiceRepository implements InvoiceRepository`
    - `PostgresNotificationRepository implements NotificationRepository`
    - `PostgresFeatureSuggestionRepository implements FeatureSuggestionRepository`
    - `PostgresDailySnapshotRepository implements DailySnapshotRepository`
    - _Requirements: 14.1–14.11_

- [ ] Checkpoint — Phase 3 complete
  - Ensure all tests pass, ask the user if questions arise.

---

## PHASE 4 — POLYMORPHISM

> Goal: Allow the same operation to behave differently depending on the runtime type. Uses strategy pattern, method overriding, and runtime dispatch.
> Demonstrates: `interface` + multiple implementations, runtime type dispatch, strategy pattern.

- [ ] POLY-01 — `NotificationSender` Strategy (Notification Dispatch)
  - [ ] POLY-01.1 Create `NotificationSender.java` interface and implementations in `notification`
    - Interface `NotificationSender`: `getChannel()`, `canSend(Notification, NotificationPreferences)`, `send(Notification)`
    - `InAppNotificationSender` — `canSend()` always returns true; `send()` creates a `NotificationDispatchRecord`
    - `EmailNotificationSender` — `canSend()` returns true only if `priority == HIGH && prefs.emailEnabled`; `send()` calls `EmailGateway.send()`
    - Update `DispatchService` to accept `List<NotificationSender>`, iterate and call each polymorphically
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5_
  - [ ]* POLY-01.2 Write property test for `NotificationSender` — `NotificationSenderTest`
    - **Property 16: NotificationSender canSend Contracts** — `InAppNotificationSender.canSend()` always returns true; `EmailNotificationSender.canSend()` returns true iff `priority == HIGH && prefs.emailEnabled`
    - **Validates: Requirements 17.2, 17.3**
  - [ ]* POLY-01.3 Write unit test for `DispatchService` — iterates all senders without modification when new sender added
    - **Validates: Requirement 17.5**

- [ ] POLY-02 — `DiscountStrategy` Strategy (Discount Calculation)
  - [ ] POLY-02.1 Create `DiscountStrategy.java` interface and implementations in `inventory-event-menagement`
    - Interface `DiscountStrategy`: `getType()`, `apply(BigDecimal, BigDecimal)`, `validate(BigDecimal)`
    - `PercentageDiscountStrategy` — `apply()` returns `basePrice * (1 - discountValue/100)` with HALF_UP; `validate()` checks 0 ≤ value ≤ 100
    - `FixedDiscountStrategy` — `apply()` returns `basePrice - discountValue`; `validate()` checks `discountValue ≤ basePrice`
    - Update `DiscountService.calculateFinalPrice()` — look up strategy by `DiscountType`, call `validate()` then `apply()`
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5, 18.6_
  - [ ]* POLY-02.2 Write property test for `DiscountStrategy` — `DiscountStrategyTest`
    - **Property 17: DiscountStrategy Apply Formulas** — `PercentageDiscountStrategy.apply()` must return `basePrice * (1 - discountValue/100)` with HALF_UP; `FixedDiscountStrategy.apply()` must return `basePrice - discountValue`; both must produce result > costPrice after `validateProfitMargin()`
    - **Validates: Requirements 18.2, 18.3, 18.6**

- [ ] POLY-03 — `SubscriptionAccount` Interface + Router
  - [ ] POLY-03.1 Create `SubscriptionAccount.java` interface and `SubscriptionActivationRouter.java` in `plan_subscription`
    - Interface `SubscriptionAccount`: `getSubscriptionId()`, `getPlanId()`, `getRegistryId()`, `activatePlan(SubscriptionPlan)`, `expire()`, `cancel()`, `isActive()`, `markExpiredIfNeeded()`
    - `UserAccount` and `ShopAccount` add `implements SubscriptionAccount`
    - `SubscriptionActivationRouter.route(TargetType, UserAccount, ShopAccount)` — returns correct `SubscriptionAccount`; unknown `TargetType` throws `DomainException`
    - Update `PlanSubscriptionEngine` to use `SubscriptionActivationRouter`
    - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5_
  - [ ]* POLY-03.2 Write unit test for `SubscriptionActivationRouter` — unknown `TargetType` throws `DomainException`
    - **Validates: Requirement 19.5**

- [ ] POLY-04 — `SnapshotAggregator` Strategy (Analytics Aggregation)
  - [ ] POLY-04.1 Create `SnapshotAggregator.java` interface and implementations in `report-and-analytic`
    - Interface `SnapshotAggregator<T>`: `getAggregatorName()`, `alreadyExists(UUID, LocalDate)`, `aggregate(UUID, LocalDate)`, `persist(T)`
    - `ProductSnapshotAggregator` — aggregates from order items + inventory data
    - `CategorySnapshotAggregator` — aggregates from product snapshots
    - `DailySnapshotAggregator` — aggregates from category snapshots
    - Update `AnalyticsAggregationService.runDailyAggregation()` to iterate aggregators in order: Product → Category → Daily
    - _Requirements: 20.1, 20.2, 20.3, 20.4, 20.5, 20.6_
  - [ ]* POLY-04.2 Write property test for `SnapshotAggregator` — `SnapshotAggregatorTest`
    - **Property 18: SnapshotAggregator Idempotency via Interface** — if `alreadyExists(shopId, date)` returns true, calling the aggregation pipeline must produce no side effects
    - **Validates: Requirement 20.6**

- [ ] POLY-05 — `ReportPeriodStrategy` Strategy (Report Period Date Ranges)
  - [ ] POLY-05.1 Create `ReportPeriodStrategy.java` interface and implementations in `report-and-analytic`
    - Interface `ReportPeriodStrategy`: `getDateRange(LocalDate)`, `getPeriodName()`, `requiresOwnerRole()`
    - `DailyReportStrategy` — single-day range, `requiresOwnerRole()` false
    - `WeeklyReportStrategy` — range from `endDate - 6 days` to `endDate`, `requiresOwnerRole()` false
    - `MonthlyReportStrategy` — range from first of month to `endDate`, `requiresOwnerRole()` false
    - `AnnualReportStrategy` — range from first of year to `endDate`, `requiresOwnerRole()` true
    - Update `ReportingService` to accept `ReportPeriodStrategy` and use `getDateRange()`
    - _Requirements: 21.1, 21.2, 21.3, 21.4, 21.5, 21.6_
  - [ ]* POLY-05.2 Write property test for `ReportPeriodStrategy` — `ReportPeriodStrategyTest`
    - **Property 19: ReportPeriodStrategy Date Range Correctness** — `WeeklyReportStrategy.getDateRange()` must return range from `endDate.minusDays(6)` to `endDate`; `AnnualReportStrategy.getDateRange()` must return range from first day of year to `endDate`
    - **Validates: Requirements 21.3, 21.5**

- [ ] POLY-06 — `PaymentProcessor` Strategy (Payment Method Processing)
  - [ ] POLY-06.1 Create `PaymentProcessor.java` interface and implementations in `payment`
    - Interface `PaymentProcessor`: `getSupportedCardType()`, `createIntent(Transaction)`, `verify(String, BigDecimal)`
    - `CardPaymentProcessor` — handles VISA, MASTERCARD, AMEX
    - `QrCodePaymentProcessor` — returns null from `getSupportedCardType()`; handles QR code generation and verification
    - Update `PaymentService` to look up processor by payment method type; if `verify()` returns false, throw `DomainException`
    - _Requirements: 22.1, 22.2, 22.3, 22.4, 22.5_
  - [ ]* POLY-06.2 Write unit test for `PaymentProcessor` — `verify()` returning false causes `PaymentService` to throw `DomainException`
    - **Validates: Requirement 22.5**

- [ ] POLY-07 — `ForecastStrategy` Strategy (Out-of-Stock Prediction)
  - [ ] POLY-07.1 Create `ForecastStrategy.java` interface and implementations in `report-and-analytic`
    - Interface `ForecastStrategy`: `getForecastType()`, `predict(List<DailyProductSnapshot>)`
    - `LinearTrendForecastStrategy` — simple linear regression on `qtySold`
    - `MovingAverageForecastStrategy` — 7-day moving average on `qtySold`
    - Update `ForecastService` to accept configurable `ForecastStrategy`; empty snapshot list returns empty list
    - _Requirements: 23.1, 23.2, 23.3, 23.4, 23.5_
  - [ ]* POLY-07.2 Write unit test for `ForecastStrategy` — empty snapshot list returns empty list without throwing
    - **Validates: Requirement 23.5**

- [ ] POLY-08 — `AccessPolicy` Interface (Role-Based Access Polymorphism)
  - [ ] POLY-08.1 Create `AccessPolicy.java` interface in `common` and refactor all policy classes
    - Interface `AccessPolicy`: `void check(Role.RoleName actorRole, String operation)`
    - `AnalyticsAccessPolicy`, `FeedbackAccessPolicy`, `NotificationAccessPolicy`, `RolePolicy` all implement `AccessPolicy`
    - Each extends `AbstractAccessPolicy` (from ABS-02) and implements `AccessPolicy`
    - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7_
  - [ ]* POLY-08.2 Write property test for `AccessPolicy` — `AccessPolicyTest`
    - **Property 20: AccessPolicy SUPER_ADMIN Always Passes** — for any `AccessPolicy` implementation, `check(SUPER_ADMIN, operation)` must not throw for any operation
    - **Validates: Requirement 24.7**

- [ ] Checkpoint — Phase 4 complete
  - Ensure all tests pass, ask the user if questions arise.

---

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at the end of each phase
- Property tests use **jqwik** (`net.jqwik:jqwik`) with `@Property` annotation; each test must reference the design property number in a comment: `// Feature: velora-oop-implementation, Property N: <title>`
- Unit tests cover specific examples and error conditions not covered by property tests
- All monetary values use `BigDecimal` with scale=2 and `HALF_UP` rounding
- All entity identifiers use `UUID`
- All domain rule violations throw `DomainException` with a descriptive message

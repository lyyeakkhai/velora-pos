# Velora Platform — OOP Implementation Task List

Ordered by OOP concept: **Inheritance → Abstraction → Interface → Polymorphism**
Each task maps to a domain, the files affected, and the OOP concept being demonstrated.
Update status as you complete each task.

---

## PHASE 1 — INHERITANCE

> Goal: Eliminate duplicate fields across entities by introducing shared abstract base classes.
> Demonstrates: code reuse through class hierarchy, `extends` keyword, `super()` constructor chaining.

---

### INH-01 — Create `AbstractEntity`
- **Domain:** common
- **File:** `src/main/java/com/velora/app/common/AbstractEntity.java`
- **What to implement:**
  - Field: `UUID id` (set in constructor, immutable)
  - Override `equals(Object)` and `hashCode()` based on `id`
  - Override `toString()` returning `ClassName{id=...}`
- **Status:** [ ] Not started

---

### INH-02 — Create `AbstractAuditableEntity`
- **Domain:** common
- **File:** `src/main/java/com/velora/app/common/AbstractAuditableEntity.java`
- **What to implement:**
  - Extends `AbstractEntity`
  - Field: `LocalDateTime createdAt` (set in constructor, immutable)
  - Field: `LocalDateTime updatedAt` (mutable)
  - Method: `protected void touch()` — sets `updatedAt = LocalDateTime.now()`
- **Status:** [ ] Not started

---

### INH-03 — Apply Inheritance to Auth Domain
- **Domain:** auth
- **Files:**
  - `src/main/java/com/velora/app/core/domain/auth/User.java`
  - `src/main/java/com/velora/app/core/domain/auth/UserAuth.java`
  - `src/main/java/com/velora/app/core/domain/auth/Membership.java`
  - `src/main/java/com/velora/app/core/domain/auth/Role.java`
- **What to implement:**
  - `User extends AbstractAuditableEntity` — remove duplicate `id`, `createdAt`, `updatedAt`
  - `UserAuth extends AbstractAuditableEntity` — remove duplicate `createdAt`
  - `Membership extends AbstractAuditableEntity` — remove duplicate timestamps, call `touch()` on updates
  - `Role extends AbstractEntity` — remove duplicate `roleId`, `equals()`, `hashCode()`
- **Status:** [ ] Not started

---

### INH-04 — Apply Inheritance to Store Domain
- **Domain:** store-management
- **Files:**
  - `src/main/java/com/velora/app/core/domain/store-management/Shop.java`
- **What to implement:**
  - `Shop extends AbstractAuditableEntity` — remove duplicate `id`, `createdAt`, `updatedAt`
  - Call `touch()` inside `transitionStatus()` and `updateAddress()`
- **Status:** [ ] Not started

---

### INH-05 — Apply Inheritance to Inventory Domain
- **Domain:** inventory-event-management
- **Files:**
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/Product.java`
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/ProductVariant.java`
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/Category.java`
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/EventType.java`
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/EventProduct.java`
- **What to implement:**
  - All five classes extend `AbstractAuditableEntity`
  - Remove duplicate `id`, `createdAt`, `updatedAt` from each
  - Call `touch()` on any mutation method
- **Status:** [ ] Not started

---

### INH-06 — Apply Inheritance to Sale Domain
- **Domain:** sale-management
- **Files:**
  - `src/main/java/com/velora/app/core/domain/sale-management/Order.java`
  - `src/main/java/com/velora/app/core/domain/sale-management/OrderItem.java`
  - `src/main/java/com/velora/app/core/domain/sale-management/Receipt.java`
  - `src/main/java/com/velora/app/core/domain/sale-management/Delivery.java`
  - `src/main/java/com/velora/app/core/domain/sale-management/PaymentIntent.java`
- **What to implement:**
  - All five classes extend `AbstractAuditableEntity`
  - Remove duplicate `id`, `createdAt`, `updatedAt` from each
- **Status:** [ ] Not started

---

### INH-07 — Apply Inheritance to Payment Domain
- **Domain:** payment
- **Files:**
  - `src/main/java/com/velora/app/core/domain/payment/Transaction.java`
  - `src/main/java/com/velora/app/core/domain/payment/Invoice.java`
  - `src/main/java/com/velora/app/core/domain/payment/PaymentMethod.java`
- **What to implement:**
  - All three classes extend `AbstractAuditableEntity`
  - Remove duplicate `id`, `createdAt`, `updatedAt` from each
- **Status:** [ ] Not started

---

### INH-08 — Apply Inheritance to Notification Domain
- **Domain:** notification
- **Files:**
  - `src/main/java/com/velora/app/core/domain/notification/Notification.java`
  - `src/main/java/com/velora/app/core/domain/notification/NotificationPreferences.java`
  - `src/main/java/com/velora/app/core/domain/notification/NotificationDispatchRecord.java`
- **What to implement:**
  - All three classes extend `AbstractAuditableEntity`
  - Remove duplicate `id`, `createdAt`, `updatedAt` from each
- **Status:** [ ] Not started

---

### INH-09 — Apply Inheritance to Feedback Domain
- **Domain:** feedback
- **Files:**
  - `src/main/java/com/velora/app/core/domain/feedback/FeatureSuggestion.java`
- **What to implement:**
  - `FeatureSuggestion extends AbstractAuditableEntity`
  - Remove duplicate `id`, `createdAt`, `updatedAt`
  - Call `touch()` inside `edit()` and `updateStatus()`
- **Status:** [ ] Not started

---

### INH-10 — Create `AbstractSubscriptionAccount`
- **Domain:** common / plan_subscription
- **File:** `src/main/java/com/velora/app/common/AbstractSubscriptionAccount.java`
- **What to implement:**
  - Fields: `subscriptionId`, `planId`, `registryId`, `status`, `startDate`, `endDate`, `refundDeadline`, `currentPlanDurationMonths`
  - Method: `calculateEndDate(LocalDate start, int months)` — returns `start.plusMonths(months)`
  - Method: `calculateRefundDeadline(LocalDate start)` — returns `start.plusDays(7)`
  - Method: `markExpiredIfNeeded()` — if `endDate.isBefore(today)` and status is ACTIVE, set EXPIRED
  - Abstract method: `boolean isActive()`
- **Status:** [ ] Not started

---

### INH-11 — Apply `AbstractSubscriptionAccount` to Subscription Entities
- **Domain:** plan_subscription
- **Files:**
  - `src/main/java/com/velora/app/core/domain/plan_subscription/UserAccount.java`
  - `src/main/java/com/velora/app/core/domain/plan_subscription/ShopAccount.java`
- **What to implement:**
  - Both extend `AbstractSubscriptionAccount`
  - Remove duplicate shared fields
  - Keep domain-specific methods: `activatePlan()`, `upgrade()`, `renew()`, `cancel()`, `expire()`
  - Implement `isActive()` — return `status == ACTIVE`
- **Status:** [ ] Not started

---

### INH-12 — Create `AbstractSubscriptionRecord`
- **Domain:** common / plan_subscription
- **File:** `src/main/java/com/velora/app/common/AbstractSubscriptionRecord.java`
- **What to implement:**
  - Fields: `subscriptionId`, `transactionId`, `planId`, `status`, `startDate`, `endDate`, `refundDeadline`
  - Method: `markRefunded()` — sets status to REFUNDED
- **Status:** [ ] Not started

---

### INH-13 — Apply `AbstractSubscriptionRecord` to Subscription Records
- **Domain:** plan_subscription
- **Files:**
  - `src/main/java/com/velora/app/core/domain/plan_subscription/UserSubscription.java`
  - `src/main/java/com/velora/app/core/domain/plan_subscription/ShopSubscription.java`
- **What to implement:**
  - Both extend `AbstractSubscriptionRecord`
  - Remove duplicate shared fields
- **Status:** [ ] Not started

---

### INH-14 — Create `AbstractSnapshot`
- **Domain:** common / report-and-analytic
- **File:** `src/main/java/com/velora/app/common/AbstractSnapshot.java`
- **What to implement:**
  - Fields (all immutable): `snapshotId` (UUID), `snapshotDate` (LocalDate), `shopId` (UUID), `createdAt` (LocalDateTime)
  - No setters for immutable fields — enforce write-once in constructor
- **Status:** [ ] Not started

---

### INH-15 — Apply `AbstractSnapshot` to Analytics Domain
- **Domain:** report-and-analytic
- **Files:**
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/DailyProductSnapshot.java`
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/DailyCategorySnapshot.java`
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/DailySnapshot.java`
- **What to implement:**
  - All three extend `AbstractSnapshot`
  - Remove duplicate `snapshotId`, `snapshotDate`, `shopId`, `createdAt`
- **Status:** [ ] Not started

---

### INH-16 — Create `AbstractDomainService`
- **Domain:** common
- **File:** `src/main/java/com/velora/app/common/AbstractDomainService.java`
- **What to implement:**
  - Method: `protected void requireRole(Role.RoleName actual, Role.RoleName... allowed)` — throws `DomainException` if not in allowed list
  - Method: `protected void requireNotNull(Object value, String fieldName)` — throws `DomainException` if null
- **Status:** [ ] Not started

---

### INH-17 — Apply `AbstractDomainService` to Domain Services
- **Domain:** notification, feedback, report-and-analytic
- **Files:**
  - `src/main/java/com/velora/app/core/domain/notification/NotificationService.java`
  - `src/main/java/com/velora/app/core/domain/feedback/FeedbackService.java`
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/ReportingService.java`
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/AnalyticsAggregationService.java`
- **What to implement:**
  - All four extend `AbstractDomainService`
  - Replace inline role checks with `requireRole(...)` calls
  - Replace inline null checks with `requireNotNull(...)` calls
- **Status:** [ ] Not started

---

## PHASE 2 — ABSTRACTION

> Goal: Hide implementation details behind abstract classes with abstract methods. Force subclasses to provide domain-specific behavior while sharing common logic.
> Demonstrates: `abstract class`, `abstract` methods, template method pattern.

---

### ABS-01 — Abstract `AbstractAccessPolicy`
- **Domain:** common
- **File:** `src/main/java/com/velora/app/common/AbstractAccessPolicy.java`
- **What to implement:**
  - Abstract method: `abstract void check(Role.RoleName actorRole, String operation)`
  - Concrete method: `void requireAdmin(Role.RoleName actorRole)` — calls `check(actorRole, "ADMIN_ONLY")`
- **Status:** [ ] Not started

---

### ABS-02 — Apply `AbstractAccessPolicy` to Domain Policies
- **Domain:** notification, feedback, inventory-event-management, report-and-analytic
- **Files:**
  - `src/main/java/com/velora/app/core/domain/notification/NotificationAccessPolicy.java`
  - `src/main/java/com/velora/app/core/domain/feedback/FeedbackAccessPolicy.java`
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/RolePolicy.java`
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/AnalyticsAccessPolicy.java`
- **What to implement:**
  - All four extend `AbstractAccessPolicy`
  - Each implements `check()` with domain-specific role rules
  - Use inherited `requireAdmin()` where applicable
- **Status:** [ ] Not started

---

### ABS-03 — Abstract `AbstractSubscriptionAccount` Lifecycle Methods
- **Domain:** plan_subscription
- **Files:**
  - `src/main/java/com/velora/app/common/AbstractSubscriptionAccount.java` (update)
  - `src/main/java/com/velora/app/core/domain/plan_subscription/UserAccount.java`
  - `src/main/java/com/velora/app/core/domain/plan_subscription/ShopAccount.java`
- **What to implement:**
  - Declare `abstract void activatePlan(SubscriptionPlan plan)` in `AbstractSubscriptionAccount`
  - Declare `abstract void cancel()` in `AbstractSubscriptionAccount`
  - `UserAccount` implements `activatePlan()` — sets user-specific fields, calls `calculateEndDate()`
  - `ShopAccount` implements `activatePlan()` — sets shop-specific fields, calls `calculateEndDate()`
  - Both implement `cancel()` with their own cancellation logic
- **Status:** [ ] Not started

---

### ABS-04 — Abstract `AbstractSnapshotAggregator`
- **Domain:** report-and-analytic
- **File:** `src/main/java/com/velora/app/common/AbstractSnapshotAggregator.java`
- **What to implement:**
  - Generic class: `AbstractSnapshotAggregator<T>`
  - Abstract method: `abstract T aggregate(UUID shopId, LocalDate date)`
  - Abstract method: `abstract void persist(T snapshot)`
  - Concrete method: `boolean alreadyExists(UUID shopId, LocalDate date)` — queries repository
  - Template method: `void run(UUID shopId, LocalDate date)` — calls `alreadyExists()` → `aggregate()` → `persist()`
- **Status:** [ ] Not started

---

### ABS-05 — Abstract `AbstractReportPeriod`
- **Domain:** report-and-analytic
- **File:** `src/main/java/com/velora/app/common/AbstractReportPeriod.java`
- **What to implement:**
  - Abstract method: `abstract DateRange getDateRange(LocalDate endDate)`
  - Abstract method: `abstract String getPeriodName()`
  - Concrete method: `PeriodReportDTO buildReport(UUID shopId, LocalDate endDate, DailySnapshotRepository repo)` — calls `getDateRange()` then queries repo
- **Status:** [ ] Not started

---

### ABS-06 — Abstract `AbstractDiscountCalculator`
- **Domain:** inventory-event-management
- **File:** `src/main/java/com/velora/app/common/AbstractDiscountCalculator.java`
- **What to implement:**
  - Abstract method: `abstract BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue)`
  - Concrete method: `void validateProfitMargin(BigDecimal finalPrice, BigDecimal costPrice)` — throws `DomainException` if `finalPrice <= costPrice`
- **Status:** [ ] Not started

---

### ABS-07 — Abstract `AbstractNotificationDispatcher`
- **Domain:** notification
- **File:** `src/main/java/com/velora/app/common/AbstractNotificationDispatcher.java`
- **What to implement:**
  - Abstract method: `abstract void send(Notification notification)`
  - Concrete method: `boolean shouldSend(Notification n, NotificationPreferences prefs)` — shared eligibility logic (billingAlerts always true, others check prefs)
  - Template method: `void dispatch(Notification n, NotificationPreferences prefs)` — calls `shouldSend()` → `send()`
- **Status:** [ ] Not started

---

## PHASE 3 — INTERFACE

> Goal: Define contracts for all repositories, gateways, and services. Domain depends only on interfaces, not implementations.
> Demonstrates: `interface` keyword, dependency inversion, loose coupling.

---

### INT-01 — Auth Repository Interfaces
- **Domain:** auth
- **Package:** `com.velora.app.core.domain.auth`
- **Files to create:**
  - `UserRepository.java` — `save(User)`, `findById(UUID)`, `findByUsername(String)`, `existsByUsername(String)`
  - `UserAuthRepository.java` — `save(UserAuth)`, `findByEmail(String)`, `findByUserId(UUID)`, `existsByEmail(String)`
  - `MembershipRepository.java` — `save(Membership)`, `findByUserId(UUID)`, `findByShopId(UUID)`, `findByUserAndShop(UUID, UUID)`
- **Status:** [ ] Not started

---

### INT-02 — Subscription Repository Interfaces
- **Domain:** plan_subscription
- **Package:** `com.velora.app.core.domain.plan_subscription`
- **Files to create:**
  - `SubscriptionPlanRepository.java` — `save(SubscriptionPlan)`, `findById(UUID)`, `findBySlug(String)`, `findAllActive()`
  - `PlatformRegistryRepository.java` — `save(PlatformRegistry)`, `findById(UUID)`, `findByOwnerId(UUID)`
  - `UserAccountRepository.java` — `save(UserAccount)`, `findByUserId(UUID)`, `findAllActive()`
  - `ShopAccountRepository.java` — `save(ShopAccount)`, `findByShopId(UUID)`, `findAllActive()`
  - `UserSubscriptionRepository.java` — `save(UserSubscription)`, `findByUserId(UUID)`
  - `ShopSubscriptionRepository.java` — `save(ShopSubscription)`, `findByShopId(UUID)`
- **Status:** [ ] Not started

---

### INT-03 — Store Repository Interface
- **Domain:** store-management
- **Package:** `com.velora.app.core.domain.store-management`
- **Files to verify/create:**
  - `ShopRepository.java` — `save(Shop)`, `findById(UUID)`, `findBySlug(String)`, `findByOwnerId(UUID)`, `existsBySlug(String)`
- **Status:** [ ] Not started

---

### INT-04 — Inventory Store Interfaces
- **Domain:** inventory-event-management
- **Package:** `com.velora.app.core.domain.inventory-event-menagement`
- **Files to verify:**
  - `ProductStore.java` — `save(Product)`, `findById(UUID)`, `findByShopId(UUID)`
  - `ProductVariantStore.java` — `save(ProductVariant)`, `findByProductId(UUID)`, `findBySku(String)`
  - `CategoryStore.java` — `save(Category)`, `findByShopId(UUID)`
  - `EventTypeStore.java` — `save(EventType)`, `findByShopId(UUID)`
  - `EventProductStore.java` — `save(EventProduct)`, `findByEventId(UUID)`, `findByProductId(UUID)`
- **Status:** [ ] Not started

---

### INT-05 — Sale Store Interfaces
- **Domain:** sale-management
- **Package:** `com.velora.app.core.domain.sale-management`
- **Files to verify:**
  - `OrderStore.java` — `save(Order)`, `findById(UUID)`, `findByShopId(UUID)`
  - `ReceiptStore.java` — `save(Receipt)`, `findByOrderId(UUID)`
  - `DeliveryStore.java` — `save(Delivery)`, `findByOrderId(UUID)`
  - `PaymentIntentStore.java` — `save(PaymentIntent)`, `findByBankRefId(String)`, `getForUpdate(UUID)`, `existsByBankRefId(String)`, `delete(UUID)`
- **Status:** [ ] Not started

---

### INT-06 — Payment Repository Interfaces
- **Domain:** payment
- **Package:** `com.velora.app.core.domain.payment`
- **Files to create:**
  - `TransactionRepository.java` — `save(Transaction)`, `findById(UUID)`, `findByGatewayRef(String)`
  - `InvoiceRepository.java` — `save(Invoice)`, `findById(UUID)`, `findByTransactionId(UUID)`
  - `PaymentMethodRepository.java` — `save(PaymentMethod)`, `findById(UUID)`
  - `PlatformRevenueSnapshotRepository.java` — `save(PlatformRevenueSnapshot)`, `findByDate(LocalDate)`
- **Status:** [ ] Not started

---

### INT-07 — Notification Repository Interfaces
- **Domain:** notification
- **Package:** `com.velora.app.core.domain.notification`
- **Files to verify:**
  - `NotificationRepository.java` — `append(Notification)`, `markRead(UUID, UUID)`, `markAllRead(UUID)`, `countUnread(UUID)`, `findUserNotifications(UUID, int, LocalDateTime)`
  - `NotificationDispatchRepository.java` — `createIfAbsent(NotificationDispatchRecord)`, `findPending()`, `save(NotificationDispatchRecord)`
  - `NotificationPreferencesRepository.java` — `save(NotificationPreferences)`, `findByUserId(UUID)`
- **Status:** [ ] Not started

---

### INT-08 — Feedback Repository Interface
- **Domain:** feedback
- **Package:** `com.velora.app.core.domain.feedback`
- **Files to verify:**
  - `FeatureSuggestionRepository.java` — `save(FeatureSuggestion)`, `findById(UUID)`, `findByUserId(UUID)`, `findByStatus(SuggestionStatus)`
- **Status:** [ ] Not started

---

### INT-09 — Analytics Repository Interfaces
- **Domain:** report-and-analytic
- **Package:** `com.velora.app.core.domain.report-and-analytic`
- **Files to verify:**
  - `DailySnapshotRepository.java` — `save(DailySnapshot)`, `findByShopAndDate(UUID, LocalDate)`, `findByShopAndDateRange(UUID, LocalDate, LocalDate)`
  - `DailyProductSnapshotRepository.java` — `save(DailyProductSnapshot)`, `findByShopAndDate(UUID, LocalDate)`
  - `DailyCategorySnapshotRepository.java` — `save(DailyCategorySnapshot)`, `findByShopAndDate(UUID, LocalDate)`
  - `AnalyticsJobLockStore.java` — `tryLock(LocalDate)`, `release(LocalDate)`
  - `ConfirmedOrderReadRepository.java` — `findConfirmedOrdersByDate(LocalDate)`
  - `InventoryAnalyticsReadRepository.java` — `findStockAtMidnight(LocalDate)`
  - `OrderItemReadRepository.java` — `findOrderItemsByDate(LocalDate)`
- **Status:** [ ] Not started

---

### INT-10 — Gateway Interfaces
- **Domain:** notification, sale-management
- **Files to verify:**
  - `notification/EmailGateway.java` — `send(String to, String subject, String body)`
  - `sale-management/WebhookSecurity.java` — `verifySignature(String payload, String signature)`, `verifyNotReplayed(String nonce)`
  - `sale-management/TransactionRunner.java` — `runInTransaction(Runnable task)`
- **Status:** [ ] Not started

---

### INT-11 — Service Interfaces (Application Layer)
- **Domain:** all
- **Package:** `com.velora.app.core.service`
- **Files to create:**
  - `IAuthService.java`
  - `ISubscriptionService.java`
  - `IStoreService.java`
  - `IInventoryManagementService.java`
  - `ISaleOrchestrationService.java`
  - `IPaymentService.java`
  - `INotificationOrchestrationService.java`
  - `IFeedbackOrchestrationService.java`
  - `IAnalyticsService.java`
  - `IRevenueService.java`
  - `IAdminService.java`
- **What to implement:** Each interface declares all public methods from the corresponding service (see service.md for method signatures)
- **Status:** [ ] Not started

---

### INT-12 — Implement `AuthService`
- **Domain:** auth
- **File:** `src/main/java/com/velora/app/core/service/auth/AuthService.java`
- **What to implement:** `AuthService implements IAuthService`
  - `registerUser(username, email, rawPassword)` — hash password, validate uniqueness, create User + UserAuth
  - `registerOAuth(username, email, provider, providerUid)` — create User + UserAuth with OAuth provider
  - `login(email, rawPassword)` — find UserAuth, verify password, return UserAuth
  - `assignMembership(actorId, userId, shopId, role)` — role check OWNER/SUPER_ADMIN, create Membership
  - `revokeMembership(actorId, membershipId)` — role check, delete Membership
  - `updateUserStatus(actorId, userId, newStatus)` — admin only, update User status
- **Status:** [ ] Not started

---

### INT-13 — Implement `SubscriptionService`
- **Domain:** plan_subscription
- **File:** `src/main/java/com/velora/app/core/service/subscription/SubscriptionService.java`
- **What to implement:** `SubscriptionService implements ISubscriptionService`
  - `onboardUser(userId, basicPlanId)` — create PlatformRegistry, create UserAccount, activate free plan
  - `activateUserPlan(userId, planId, transactionId)` — load UserAccount, call `activatePlan(plan)`
  - `activateShopPlan(shopId, planId, transactionId)` — load ShopAccount, call `activatePlan(plan)`
  - `upgradeUserPlan(userId, newPlanId)` — load UserAccount, call `upgrade(plan)`
  - `cancelUserSubscription(userId)` — load UserAccount, call `cancel()`
  - `runExpirationJob()` — load all ACTIVE accounts, call `markExpiredIfNeeded()` on each
  - `userHasFeature(userId, featureKey)` — load UserAccount plan, call `hasFeature(featureKey)`
  - `banRegistry(actorId, registryId, reason)` — admin only, call `PlatformRegistry.ban(reason)`
- **Status:** [ ] Not started

---

### INT-14 — Implement `StoreService`
- **Domain:** store-management
- **File:** `src/main/java/com/velora/app/core/service/store/StoreService.java`
- **What to implement:** `StoreService implements IStoreService`
  - `registerShop(ownerId, slug, address)` — verify owner has active UserAccount, create Shop
  - `verifyShop(actorId, shopId, legalName, taxId)` — set legal info, call `transitionStatus(VERIFIED)`
  - `activateShop(actorId, shopId)` — call `transitionStatus(ACTIVE, false)`
  - `suspendShop(actorId, shopId)` — call `transitionStatus(SUSPENDED)`
  - `banShop(actorId, shopId)` — SUPER_ADMIN only, call `transitionStatus(BANNED)`
  - `unbanShop(actorId, shopId)` — call `transitionStatus(ACTIVE, adminOverride=true)`
  - `updateAddress(actorId, shopId, newAddress)` — role check, update address, call `touch()`
  - `calculatePayout(shopId, from, to)` — aggregate confirmed orders in date range
- **Status:** [ ] Not started

---

### INT-15 — Implement `InventoryManagementService`
- **Domain:** inventory-event-management
- **File:** `src/main/java/com/velora/app/core/service/inventory/InventoryManagementService.java`
- **What to implement:** `InventoryManagementService implements IInventoryManagementService`
  - `createProductAtomic(actorId, shopId, name, slug, basePrice, costPrice, categoryId, variants)` — role check OWNER/MANAGER, run in transaction: create Product → bulk insert variants
  - `updateProduct(actorId, shopId, productId, name, basePrice)` — role check, update fields, call `touch()`
  - `bulkInsertVariants(actorId, shopId, productId, variants)` — role check, validate SKU uniqueness, save all
  - `createEvent(actorId, shopId, name, discountValue, discountType, startDate, endDate)` — role check, create EventType
  - `attachProductToEvent(actorId, shopId, productId, eventId)` — role check, create EventProduct
  - `calculateFinalPrice(shopId, productId, eventId)` — load product + event, apply discount strategy, validate profit margin
  - `createCategory(actorId, shopId, name)` — role check, create Category
- **Status:** [ ] Not started

---

### INT-16 — Implement `SaleOrchestrationService`
- **Domain:** sale-management
- **File:** `src/main/java/com/velora/app/core/service/sale/SaleOrchestrationService.java`
- **What to implement:** `SaleOrchestrationService implements ISaleOrchestrationService`
  - `createPaymentIntent(shopId, customerId, items, bankRefId)` — validate cart, snapshot prices, create PaymentIntent
  - `handlePaymentWebhook(bankRefId, paidAmount, payload, signature, nonce)` — verify signature + nonce, find intent, finalize order
  - `finalizeOrder(intentId, deliveryNeeded, deliveryAddress)` — run in transaction: create Order + OrderItems + Receipt + optional Delivery
  - `expireStaleIntents(intentIds)` — mark each PaymentIntent as EXPIRED
  - `cancelUnpaidOrders(orderIds)` — mark each Order as CANCELLED
- **Status:** [ ] Not started

---

### INT-17 — Implement `PaymentService`
- **Domain:** payment
- **File:** `src/main/java/com/velora/app/core/service/payment/PaymentService.java`
- **What to implement:** `PaymentService implements IPaymentService`
  - `createTransaction(amount, currency, payerType, payerId, planId)` — create Transaction in PENDING state
  - `markTransactionPaid(transactionId, gatewayRef)` — call `Transaction.markPaid(gatewayRef)`
  - `markTransactionFailed(transactionId)` — call `Transaction.markFailed()`
  - `issueInvoice(transactionId, invoiceNo, subTotal, taxAmount, discountPrice)` — call `Invoice.verifyTotal()`, save
  - `cancelInvoice(invoiceId)` — call `Invoice.cancel()`
  - `registerPaymentMethod(gatewayToken, cardType, lastFour, expiryDate)` — create PaymentMethod
  - `generateDailySnapshot(date)` — aggregate PAID transactions for date, create PlatformRevenueSnapshot
- **Status:** [ ] Not started

---

### INT-18 — Implement `NotificationOrchestrationService`
- **Domain:** notification
- **File:** `src/main/java/com/velora/app/core/service/notification/NotificationOrchestrationService.java`
- **What to implement:** `NotificationOrchestrationService implements INotificationOrchestrationService`
  - `sendNotification(actorRole, systemActor, userId, type, priority, title, content, linkUrl)` — role check, create Notification, delegate to DispatchService
  - `markRead(actorUserId, userId, notificationId)` — access check, call NotificationService.markRead()
  - `markAllRead(actorUserId, userId)` — access check, call NotificationService.markAllRead()
  - `getUnreadCount(actorUserId, userId)` — access check, return count
  - `getNotifications(actorUserId, userId, limit, before)` — access check, return paginated list
  - `updatePreferences(userId, emailEnabled, marketingAlerts)` — load or create prefs, update, save
  - `retryFailedDispatches()` — load PENDING dispatch records, retry each via DispatchService
- **Status:** [ ] Not started

---

### INT-19 — Implement `FeedbackOrchestrationService`
- **Domain:** feedback
- **File:** `src/main/java/com/velora/app/core/service/feedback/FeedbackOrchestrationService.java`
- **What to implement:** `FeedbackOrchestrationService implements IFeedbackOrchestrationService`
  - `submitSuggestion(userId, category, problemText, solutionText)` — create FeatureSuggestion in NEW status
  - `editSuggestion(actorUserId, suggestionId, category, problemText, solutionText)` — ownership check, call `suggestion.edit()`
  - `updateStatus(suggestionId, newStatus, adminNotes, actorRole)` — SUPER_ADMIN only, call `suggestion.updateStatus()`
  - `listMySuggestions(userId)` — return suggestions by userId
  - `adminListByStatus(status, actorRole)` — SUPER_ADMIN only, return suggestions by status
- **Status:** [ ] Not started

---

### INT-20 — Implement `AnalyticsService`
- **Domain:** report-and-analytic
- **File:** `src/main/java/com/velora/app/core/service/analytics/AnalyticsService.java`
- **What to implement:** `AnalyticsService implements IAnalyticsService`
  - `runDailyAggregation(date)` — acquire job lock, run each SnapshotAggregator in order (product → category → daily), release lock
  - `getDailyReport(actorRole, shopId, date)` — role check, load DailySnapshot, return DailyReportDTO
  - `getWeeklyReport(actorRole, shopId, endDate)` — role check, use WeeklyReportStrategy, return PeriodReportDTO
  - `getMonthlyReport(actorRole, shopId, endDate)` — role check, use MonthlyReportStrategy
  - `getAnnualReport(actorRole, shopId, endDate)` — OWNER only, use AnnualReportStrategy
  - `rankSellers(actorRole, shopId, range)` — OWNER/MANAGER, aggregate from snapshots
  - `getCategoryTrends(actorRole, shopId, range)` — OWNER/MANAGER, aggregate from category snapshots
  - `predictOutOfStock(actorRole, shopId)` — delegate to ForecastService with configured strategy
- **Status:** [ ] Not started

---

### INT-21 — Implement `RevenueService`
- **Domain:** revenue
- **File:** `src/main/java/com/velora/app/core/service/revenue/RevenueService.java`
- **What to implement:** `RevenueService implements IRevenueService`
  - `generateDailySnapshot(date)` — aggregate PAID transactions for date, create PlatformRevenueSnapshot in DRAFT
  - `getSnapshotByDate(date)` — load snapshot by date
  - `getRangeSummary(from, to)` — load all snapshots in range
  - `getYearlyReport(year)` — aggregate monthly totals for the year
  - `finalizeSnapshot(actorId, snapshotId)` — admin only, transition DRAFT → FINALIZED
  - `lockSnapshot(snapshotId)` — system only, transition FINALIZED → LOCKED
- **Status:** [ ] Not started

---

### INT-22 — Implement `AdminService`
- **Domain:** admin-userSetting
- **File:** `src/main/java/com/velora/app/core/service/admin/AdminService.java`
- **What to implement:** `AdminService implements IAdminService`
  - `banUser(actorId, userId, reason)` — SUPER_ADMIN only, call `User.ban(reason)`, audit log
  - `revokeUserSessions(actorId, userId)` — SUPER_ADMIN only, invalidate all tokens
  - `banShop(actorId, shopId, reason)` — SUPER_ADMIN only, call `Shop.transitionStatus(BANNED)`
  - `unbanShop(actorId, shopId)` — SUPER_ADMIN only, call `Shop.transitionStatus(ACTIVE, adminOverride=true)`
  - `viewRevenueSnapshots(actorId, from, to)` — SUPER_ADMIN only, delegate to RevenueService
  - `changePermissions(actorId, membershipId, newRole)` — SUPER_ADMIN only, update Membership role
- **Status:** [ ] Not started

---

### INT-23 — Implement Infrastructure Repositories (PostgreSQL)
- **Domain:** all
- **Package:** `com.velora.app.infrastructure.db`
- **What to implement:** One `Postgres*` class per repository interface
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
- **Status:** [ ] Not started

---

## PHASE 4 — POLYMORPHISM

> Goal: Allow the same operation to behave differently depending on the runtime type. Uses strategy pattern, method overriding, and runtime dispatch.
> Demonstrates: `interface` + multiple implementations, runtime type dispatch, strategy pattern.

---

### POLY-01 — `NotificationSender` Strategy (Notification Dispatch)
- **Domain:** notification
- **Files:**
  - `src/main/java/com/velora/app/core/domain/notification/NotificationSender.java` (new interface)
  - `src/main/java/com/velora/app/core/domain/notification/InAppNotificationSender.java` (new)
  - `src/main/java/com/velora/app/core/domain/notification/EmailNotificationSender.java` (new)
  - `src/main/java/com/velora/app/core/domain/notification/DispatchService.java` (update)
- **What to implement:**
  - Interface `NotificationSender`:
    - `NotificationChannel getChannel()`
    - `boolean canSend(Notification n, NotificationPreferences prefs)`
    - `void send(Notification n)`
  - `InAppNotificationSender` — `canSend()` always returns true; `send()` creates a `NotificationDispatchRecord`
  - `EmailNotificationSender` — `canSend()` returns true only if `priority == HIGH && prefs.emailEnabled`; `send()` calls `EmailGateway.send()`
  - `DispatchService` — accepts `List<NotificationSender>`, iterates and calls each polymorphically
- **Status:** [ ] Not started

---

### POLY-02 — `DiscountStrategy` Strategy (Discount Calculation)
- **Domain:** inventory-event-management
- **Files:**
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/DiscountStrategy.java` (new interface)
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/PercentageDiscountStrategy.java` (new)
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/FixedDiscountStrategy.java` (new)
  - `src/main/java/com/velora/app/core/domain/inventory-event-menagement/DiscountService.java` (update)
- **What to implement:**
  - Interface `DiscountStrategy`:
    - `DiscountType getType()`
    - `BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue)`
    - `void validate(BigDecimal discountValue)`
  - `PercentageDiscountStrategy` — `apply()` returns `basePrice * (1 - discountValue/100)`; `validate()` checks 0 ≤ value ≤ 100
  - `FixedDiscountStrategy` — `apply()` returns `basePrice - discountValue`; `validate()` checks `discountValue ≤ basePrice`
  - `DiscountService.calculateFinalPrice()` — look up strategy by `DiscountType`, call `strategy.validate()` then `strategy.apply()`
- **Status:** [ ] Not started

---

### POLY-03 — `SubscriptionAccount` Interface + Router
- **Domain:** plan_subscription
- **Files:**
  - `src/main/java/com/velora/app/core/domain/plan_subscription/SubscriptionAccount.java` (new interface)
  - `src/main/java/com/velora/app/core/domain/plan_subscription/SubscriptionActivationRouter.java` (new)
  - `src/main/java/com/velora/app/core/domain/plan_subscription/UserAccount.java` (add `implements SubscriptionAccount`)
  - `src/main/java/com/velora/app/core/domain/plan_subscription/ShopAccount.java` (add `implements SubscriptionAccount`)
  - `src/main/java/com/velora/app/core/domain/plan_subscription/PlanSubscriptionEngine.java` (update)
- **What to implement:**
  - Interface `SubscriptionAccount`:
    - `UUID getSubscriptionId()`, `UUID getPlanId()`, `UUID getRegistryId()`
    - `void activatePlan(SubscriptionPlan plan)`, `void expire()`, `void cancel()`
    - `boolean isActive()`, `void markExpiredIfNeeded()`
  - `SubscriptionActivationRouter` — `route(TargetType, UserAccount, ShopAccount)` returns the correct `SubscriptionAccount` based on `TargetType`
  - `PlanSubscriptionEngine` — uses `SubscriptionActivationRouter` to get the right account, calls methods on `SubscriptionAccount` interface
- **Status:** [ ] Not started

---

### POLY-04 — `SnapshotAggregator` Strategy (Analytics Aggregation)
- **Domain:** report-and-analytic
- **Files:**
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/SnapshotAggregator.java` (new interface)
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/ProductSnapshotAggregator.java` (new)
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/CategorySnapshotAggregator.java` (new)
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/DailySnapshotAggregator.java` (new)
  - `src/main/java/com/velora/app/core/domain/report-and-analytic/AnalyticsAggregationServi
# Velora Platform — Service Implementation Guide

This document describes the service layer implementation plan for each domain feature. Services orchestrate domain entities, enforce business rules, and coordinate with repositories.

---

## Package Structure

```
com.velora.app.core.service/
├── auth/
│   └── AuthService.java
├── subscription/
│   └── SubscriptionService.java
├── store/
│   └── StoreService.java
├── inventory/
│   └── InventoryManagementService.java
├── sale/
│   └── SaleOrchestrationService.java
├── payment/
│   └── PaymentService.java
├── notification/
│   └── NotificationOrchestrationService.java
├── feedback/
│   └── FeedbackOrchestrationService.java
├── analytics/
│   └── AnalyticsService.java
├── revenue/
│   └── RevenueService.java
└── admin/
    └── AdminService.java
```

---

## 1. AuthService

**Domain:** `auth`
**Responsibility:** User registration, login, membership management

### Methods

```java
// Register a new user with email/password
User registerUser(String username, String email, String rawPassword);

// Register via OAuth provider
User registerOAuth(String username, String email, Provider provider, String providerUid);

// Authenticate user by email/password — returns UserAuth on success
UserAuth login(String email, String rawPassword);

// Assign a role to a user in a shop
Membership assignMembership(UUID actorId, UUID userId, UUID shopId, Role.RoleName role);

// Remove a user from a shop
void revokeMembership(UUID actorId, UUID membershipId);

// Suspend or delete a user (admin only)
User updateUserStatus(UUID actorId, UUID userId, User.Status newStatus);
```

### Implementation Notes
- Hash password with bcrypt before passing to `UserAuth`
- Validate email uniqueness before creating `UserAuth`
- Only OWNER or SUPER_ADMIN can assign memberships
- Use `ValidationUtils` for all input validation

---

## 2. SubscriptionService

**Domain:** `plan_subscription`
**Responsibility:** Plan lifecycle, account activation, expiration jobs

### Methods

```java
// Onboard a new user — creates PlatformRegistry + assigns basic plan
UserAccount onboardUser(UUID userId, UUID basicPlanId);

// Activate a paid plan for a user
UserAccount activateUserPlan(UUID userId, UUID planId, UUID transactionId);

// Activate a paid plan for a shop
ShopAccount activateShopPlan(UUID shopId, UUID planId, UUID transactionId);

// Upgrade a user to a new plan
UserAccount upgradeUserPlan(UUID userId, UUID newPlanId);

// Upgrade a shop to a new plan
ShopAccount upgradeShopPlan(UUID shopId, UUID newPlanId);

// Cancel a user subscription
UserAccount cancelUserSubscription(UUID userId);

// Cancel a shop subscription
ShopAccount cancelShopSubscription(UUID shopId);

// Cron job: expire all overdue accounts
int runExpirationJob();

// Check if a user has a specific feature
boolean userHasFeature(UUID userId, String featureKey);

// Check if a shop has a specific feature
boolean shopHasFeature(UUID shopId, String featureKey);

// Ban a registry entry (admin only)
PlatformRegistry banRegistry(UUID actorId, UUID registryId, String reason);
```

### Implementation Notes
- `onboardUser` auto-creates `PlatformRegistry` with PENDING status, then activates on free plan
- `activateUserPlan` calls `PlatformRegistry.activate()` then `UserAccount.activatePlan(plan)`
- `runExpirationJob` iterates all ACTIVE accounts and calls `markExpiredIfNeeded()`
- Feature checks load `SubscriptionPlan` and call `hasFeature(featureKey)`

---

## 3. StoreService

**Domain:** `store-management`
**Responsibility:** Shop registration, status management, settings

### Methods

```java
// Register a new shop (requires active UserAccount)
Shop registerShop(UUID ownerId, String slug, Address address);

// Complete shop verification (set legal name + tax ID)
Shop verifyShop(UUID actorId, UUID shopId, String legalName, String taxId);

// Activate a verified shop
Shop activateShop(UUID actorId, UUID shopId);

// Suspend a shop
Shop suspendShop(UUID actorId, UUID shopId);

// Ban a shop (admin only)
Shop banShop(UUID actorId, UUID shopId);

// Unban a shop (admin override)
Shop unbanShop(UUID actorId, UUID shopId);

// Update shop address
Shop updateAddress(UUID actorId, UUID shopId, Address newAddress);

// Update shop settings
ShopSettings updateSettings(UUID actorId, UUID shopId, ShopSettings settings);

// Calculate payout for a shop
BigDecimal calculatePayout(UUID shopId, LocalDate from, LocalDate to);
```

### Implementation Notes
- `registerShop` checks that the owner has an active `UserAccount`
- `activateShop` calls `Shop.transitionStatus(ACTIVE, false)` which enforces legalName + taxId
- `banShop` requires SUPER_ADMIN role
- `unbanShop` passes `adminOverride = true` to `transitionStatus`

---

## 4. InventoryManagementService

**Domain:** `inventory-event-management`
**Responsibility:** Product catalog, variants, discounts, events

### Methods

```java
// Create product with variants atomically
Product createProductAtomic(UUID actorId, UUID shopId, String name, String slug,
    BigDecimal basePrice, BigDecimal costPrice, UUID categoryId,
    List<VariantRequest> variants);

// Update product details
Product updateProduct(UUID actorId, UUID shopId, UUID productId, String name, BigDecimal basePrice);

// Disable a product
void disableProduct(UUID actorId, UUID shopId, UUID productId);

// Bulk insert variants
List<ProductVariant> bulkInsertVariants(UUID actorId, UUID shopId, UUID productId,
    List<VariantRequest> variants);

// Create a discount event
EventType createEvent(UUID actorId, UUID shopId, String name, BigDecimal discountValue,
    DiscountType discountType, LocalDateTime startDate, LocalDateTime endDate);

// Attach a product to an event
EventProduct attachProductToEvent(UUID actorId, UUID shopId, UUID productId, UUID eventId);

// Calculate final price after discount
BigDecimal calculateFinalPrice(UUID shopId, UUID productId, UUID eventId);

// Create a category
Category createCategory(UUID actorId, UUID shopId, String name);
```

### Implementation Notes
- `createProductAtomic` runs inside a transaction: validate role → insert product → bulk insert variants → commit
- Role check: OWNER or MANAGER only for mutations; SELLER is read-only
- `calculateFinalPrice` calls `DiscountService.validateDiscount()` then applies discount
- Profit protection: reject if `finalPrice <= costPrice`
- All queries must include `shopId` filter

---

## 5. SaleOrchestrationService

**Domain:** `sale-management`
**Responsibility:** Payment intent, order finalization, delivery

### Methods

```java
// Create a payment intent from cart
PaymentIntent createPaymentIntent(UUID shopId, UUID customerId,
    List<CartItemRequest> items, String bankRefId);

// Handle incoming payment webhook
Order handlePaymentWebhook(String bankRefId, BigDecimal paidAmount,
    String payload, String signature, String nonce);

// Finalize order atomically
Order finalizeOrder(UUID intentId, boolean deliveryNeeded, String deliveryAddress);

// Expire stale payment intents
int expireStaleIntents(List<UUID> intentIds);

// Cancel unpaid orders
int cancelUnpaidOrders(List<UUID> orderIds);

// Mark failed deliveries
int markFailedDeliveries(List<UUID> orderIds, String reason);

// Reconcile payments (idempotent replay)
int reconcilePayments(List<WebhookEvent> events);
```

### Implementation Notes
- Delegates to `OrderService` for all atomic operations
- `handlePaymentWebhook` calls `verifyPayment()` then `finalizeOrderAtomic()`
- Webhook must be idempotent — duplicate `bankRefId` is rejected
- Cart snapshot is locked at intent creation — no price re-fetch

---

## 6. PaymentService

**Domain:** `payment`
**Responsibility:** Transactions, invoices, payment methods

### Methods

```java
// Create a transaction for a subscription payment
Transaction createTransaction(BigDecimal amount, Currency currency,
    PayerType payerType, UUID payerId, UUID planId);

// Mark a transaction as paid
Transaction markTransactionPaid(UUID transactionId, String gatewayRef);

// Mark a transaction as failed
Transaction markTransactionFailed(UUID transactionId);

// Issue an invoice for a transaction
Invoice issueInvoice(UUID transactionId, String invoiceNo,
    BigDecimal subTotal, BigDecimal taxAmount, BigDecimal discountPrice);

// Cancel an invoice
Invoice cancelInvoice(UUID invoiceId);

// Register a payment method
PaymentMethod registerPaymentMethod(String gatewayToken, CardType cardType,
    String lastFour, LocalDate expiryDate);

// Generate daily platform revenue snapshot
PlatformRevenueSnapshot generateDailySnapshot(LocalDate date);
```

### Implementation Notes
- `issueInvoice` calls `Invoice.verifyTotal()` before persisting
- `markTransactionPaid` calls `Transaction.markPaid()` which enforces PENDING → PAID transition
- `generateDailySnapshot` aggregates all PAID transactions for the given date

---

## 7. NotificationOrchestrationService

**Domain:** `notification`
**Responsibility:** Notification creation, dispatch, preferences

### Methods

```java
// Send a notification to a user
Notification sendNotification(Role.RoleName actorRole, boolean systemActor,
    UUID userId, NotificationType type, NotificationPriority priority,
    String title, String content, String linkUrl);

// Mark a notification as read
void markRead(UUID actorUserId, UUID userId, UUID notificationId);

// Mark all notifications as read
int markAllRead(UUID actorUserId, UUID userId);

// Get unread count
long getUnreadCount(UUID actorUserId, UUID userId);

// Get paginated notifications
List<Notification> getNotifications(UUID actorUserId, UUID userId,
    int limit, LocalDateTime before);

// Update notification preferences
NotificationPreferences updatePreferences(UUID userId, boolean emailEnabled,
    boolean marketingAlerts);

// Retry failed dispatches
int retryFailedDispatches();
```

### Implementation Notes
- Delegates to `NotificationService` for inbox operations
- Delegates to `DispatchService` for email delivery
- Email only sent for HIGH priority + emailEnabled = true
- `billingAlerts` cannot be disabled — enforced in `NotificationPreferences`

---

## 8. FeedbackOrchestrationService

**Domain:** `feedback`
**Responsibility:** Suggestion submission and admin review

### Methods

```java
// Submit a new suggestion
FeatureSuggestion submitSuggestion(UUID userId, SuggestionCategory category,
    String problemText, String solutionText);

// Edit own suggestion (only while NEW)
FeatureSuggestion editSuggestion(UUID actorUserId, UUID suggestionId,
    SuggestionCategory category, String problemText, String solutionText);

// Admin: update suggestion status
FeatureSuggestion updateStatus(UUID suggestionId, SuggestionStatus newStatus,
    String adminNotes, Role.RoleName actorRole);

// List own suggestions
List<FeatureSuggestion> listMySuggestions(UUID userId);

// Admin: list by status
List<FeatureSuggestion> adminListByStatus(SuggestionStatus status, Role.RoleName actorRole);
```

### Implementation Notes
- Delegates entirely to `FeedbackService`
- Role check: only SUPER_ADMIN can call `updateStatus`
- Users can only see their own suggestions

---

## 9. AnalyticsService

**Domain:** `report-and-analytic`
**Responsibility:** Snapshot aggregation and reporting

### Methods

```java
// Run daily aggregation job (cron)
void runDailyAggregation(LocalDate date);

// Get daily report for a shop
DailyReportDTO getDailyReport(Role.RoleName actorRole, UUID shopId, LocalDate date);

// Get weekly report
PeriodReportDTO getWeeklyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDate);

// Get monthly report
PeriodReportDTO getMonthlyReport(Role.RoleName actorRole, UUID shopId, LocalDate endDate);

// Get annual report
PeriodReportDTO getAnnualReport(Role.RoleName actorRole, UUID shopId, LocalDate endDate);

// Rank sellers by performance
List<SellerRankDTO> rankSellers(Role.RoleName actorRole, UUID shopId, DateRange range);

// Get category trends
List<CategoryTrendDTO> getCategoryTrends(Role.RoleName actorRole, UUID shopId, DateRange range);

// Predict out-of-stock products
List<OutOfStockPredictionDTO> predictOutOfStock(Role.RoleName actorRole, UUID shopId);
```

### Implementation Notes
- `runDailyAggregation` is idempotent — skips if snapshot already exists for the date
- All reports read from snapshots only — no raw order scanning
- Role enforcement: OWNER = full access, MANAGER = category/seller, SELLER = personal only

---

## 10. RevenueService

**Domain:** `revenue`
**Responsibility:** Platform-level revenue tracking

### Methods

```java
// Generate daily platform revenue snapshot (cron)
PlatformRevenueSnapshot generateDailySnapshot(LocalDate date);

// Get snapshot by date
PlatformRevenueSnapshot getSnapshotByDate(LocalDate date);

// Get range summary
List<PlatformRevenueSnapshot> getRangeSummary(LocalDate from, LocalDate to);

// Get yearly report
Map<LocalDate, BigDecimal> getYearlyReport(int year);

// Finalize a snapshot (admin)
PlatformRevenueSnapshot finalizeSnapshot(UUID actorId, UUID snapshotId);

// Lock a snapshot (system)
PlatformRevenueSnapshot lockSnapshot(UUID snapshotId);
```

### Implementation Notes
- Only ADMIN/SYSTEM can generate or lock snapshots
- DRAFT snapshots can be recalculated; FINALIZED and LOCKED are read-only
- Aggregates from `transactions` (PAID), `platform_registry` (active shops), and `daily_snapshots`

---

## 11. AdminService

**Domain:** `admin-userSetting`
**Responsibility:** Platform governance and admin operations

### Methods

```java
// Ban a user (admin only)
User banUser(UUID actorId, UUID userId, String reason);

// Revoke all sessions for a user
void revokeUserSessions(UUID actorId, UUID userId);

// Ban a shop (admin only)
Shop banShop(UUID actorId, UUID shopId, String reason);

// Unban a shop with admin override
Shop unbanShop(UUID actorId, UUID shopId);

// View platform revenue snapshots
List<PlatformRevenueSnapshot> viewRevenueSnapshots(UUID actorId, LocalDate from, LocalDate to);

// Monitor data sync logs
List<DataSyncLog> getDataSyncLogs(UUID actorId, LocalDate date);

// Change user permissions
Membership changePermissions(UUID actorId, UUID membershipId, Role.RoleName newRole);
```

### Implementation Notes
- All methods require SUPER_ADMIN role
- Audit log every admin action
- Session revocation invalidates all active tokens for the user

---

## Shared Service Patterns

### Transaction Handling
All atomic operations use `TransactionRunner.runInTransaction()`:
```java
transactionRunner.runInTransaction(() -> {
    // all steps inside one DB transaction
});
```

### Role Enforcement Pattern
```java
// Check role before any mutation
if (actorRole != Role.RoleName.OWNER && actorRole != Role.RoleName.MANAGER) {
    throw new IllegalStateException("Insufficient permissions");
}
```

### Validation Pattern
```java
// Always validate inputs first
ValidationUtils.validateUUID(userId, "userId");
ValidationUtils.validateNotBlank(name, "name");
ValidationUtils.normalizeMoney(price, "price");
```

### Repository Pattern
All services depend on repository interfaces (not implementations):
```java
// Domain interface
public interface ProductStore {
    Product save(Product product);
    Optional<Product> findById(UUID productId);
    List<Product> findByShopId(UUID shopId);
}

// Infrastructure implementation (separate layer)
public class PostgresProductStore implements ProductStore { ... }
```

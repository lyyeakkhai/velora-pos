# Velora Platform — Domain Design

## Architecture Overview

Velora follows Clean Architecture with Domain-Driven Design (DDD). The domain layer contains all business logic, entities, value objects, and domain services. Infrastructure concerns (persistence, email, UI) are isolated behind interfaces defined in the domain.

```
┌─────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                             │
│  Entities · Value Objects · Domain Services · Repositories  │
├─────────────────────────────────────────────────────────────┤
│                  APPLICATION LAYER                          │
│         Use Cases · Orchestration · DTOs                    │
├─────────────────────────────────────────────────────────────┤
│                 INFRASTRUCTURE LAYER                        │
│    PostgreSQL · Email Gateway · Console UI · Config         │
└─────────────────────────────────────────────────────────────┘
```

---

## Domain 1: Authentication (auth)

### Aggregate Root: User
Manages user profile and lifecycle state.

**Entities:**
- `User` — profile (username, profileUrl, bio, status: ACTIVE/SUSPENDED/DELETED)
- `UserAuth` — credentials (email, passwordHash, provider: EMAIL/GOOGLE/FACEBOOK, providerUid)
- `Membership` — user-to-shop access with role assignment (sellerName, shopId, roleId)
- `Role` — permission classification (SUPER_ADMIN, OWNER, MANAGER, SELLER)

**Invariants:**
- Username: 3–30 chars, alphanumeric + underscore
- Email: RFC5322 format
- Password: bcrypt hash (≥60 chars, $2a/$2b/$2y prefix)
- Membership timestamps: not future, auto-managed
- Role must be a valid enum value

**Relationships:**
- User (1:1) UserAuth
- User (1:N) Membership
- Role (1:N) Membership

**State Transitions:**
```
User:    ACTIVE → SUSPENDED → DELETED
```

---

## Domain 2: Plan & Subscription (plan_subscription)

### Aggregate Root: PlatformRegistry
Master access switch for the entire platform.

**Entities:**
- `SubscriptionPlan` — plan catalog (name, slug, price, durationMonths, payerType: USER/SHOP)
- `Feature` — feature catalog (featureKey, targetType: USER/SHOP/BOTH, description)
- `PlanFeature` — bridge (planId, featureId, limitValue, isEnabled)
- `PlatformRegistry` — master switch (ownerId, targetType, status, banReason, transactionId)
- `UserAccount` — user subscription lifecycle (userId, planId, registryId, status, dates)
- `ShopAccount` — shop subscription lifecycle (shopId, planId, registryId, status, autoRenew, dates)
- `UserSubscription` — user subscription record (userId, transactionId, planId, status, dates)
- `ShopSubscription` — shop subscription record (shopId, transactionId, planId, status, dates)

**Invariants:**
- price ≥ 0, durationMonths > 0
- slug: lowercase, URL-safe
- featureKey: valid identifier format
- startDate < endDate
- refundDeadline = startDate + 14 days
- BANNED registry cannot return to ACTIVE

**State Transitions:**
```
PlatformRegistry: PENDING → ACTIVE → INACTIVE
                  ANY → BANNED (terminal without admin)

UserAccount:  TRIAL/ACTIVE → EXPIRED → CANCELLED
ShopAccount:  ACTIVE → EXPIRED → CANCELLED / PAST_DUE
Subscription: ACTIVE → EXPIRED → CANCELLED / REFUNDED
```

**Key Services:**
- `PlanSubscriptionEngine` — routes activation to UserAccount or ShopAccount

---

## Domain 3: Store Management (store-management)

### Aggregate Root: Shop
Manages vendor shop lifecycle and identity.

**Entities:**
- `Shop` — aggregate root (shopId, ownerId, legalName, taxId, slug, status, physicalAddress)
- `Address` — value object (street, city, district, province — Cambodian provinces)
- `ShopAccount` — subscription reference
- `ShopSettings` — shop configuration (currency, timezone, etc.)
- `ShopRepository` — persistence interface

**Invariants:**
- Shop cannot be ACTIVE without legalName and taxId
- Slug must be unique and URL-safe lowercase
- BANNED shops cannot revert without adminOverride = true
- Province must match official Cambodian provinces

**State Transitions:**
```
Shop: PENDING → ACTIVE → SUSPENDED → BANNED
      BANNED → ACTIVE (admin override only)
```

**Key Services:**
- `StoreManagementService` — registerShop(), updateShopStatus(), payoutCalculation()

---

## Domain 4: Inventory & Event Management (inventory-event-management)

### Aggregate Root: Product
Manages product catalog, variants, and promotional events.

**Entities:**
- `Product` — catalog item (productId, name, slug, basePrice, costPrice, categoryId, shopId)
- `ProductVariant` — variant (variantId, productId, size, color, stockQuantity, sku, shopId)
- `Category` — product grouping (categoryId, name, shopId)
- `EventType` — discount rule (eventId, name, discountValue, discountType: PERCENTAGE/FIXED, dateRange, usageLimit)
- `EventProduct` — junction (eventProductId, productId, categoryId, eventId, status, shopId)

**Invariants:**
- basePrice > 0, costPrice ≤ basePrice
- stockQuantity ≥ 0
- SKU: uppercase alphanumeric + hyphen/underscore, 3–32 chars
- PERCENTAGE discount ≤ 100
- finalPrice > costPrice (profit protection)
- All entities scoped by shopId (row-level security)

**State Transitions:**
```
EventProduct: SCHEDULED → ACTIVE → ENDED (terminal)
```

**Key Services:**
- `ProductService` — createProductAtomic(), updateProduct(), bulkInsertVariants()
- `CategoryService` — createCategory(), validateOwnership()
- `DiscountService` — createEvent(), attachProductToEvent(), validateDiscount(), calculateFinalPrice()

---

## Domain 5: Sale Management (sale-management)

### Aggregate Root: Order
Manages the complete order and payment lifecycle.

**Entities:**
- `Order` — permanent sale record (orderId, status, totalPrice, shopId, customerId, items)
- `OrderItem` — line item (orderItemId, orderId, productId, quantity, soldPrice, subtotal)
- `Receipt` — payment proof (receiptId, orderId, receiptNumber: INV-XXXX, isPaid, bankTransactionRef)
- `Delivery` — logistics (deliveryId, orderId, status, address, completedAt)
- `PaymentIntent` — temporary pre-payment record (intentId, bankRefId, shopId, customerId, cartSnapshot, totalAmount)

**Invariants:**
- totalPrice = sum of item subtotals
- quantity > 0, soldPrice ≥ 0
- receiptNumber format: INV-XXXX
- cartSnapshot is immutable after creation
- bankRefId must be unique (idempotency)
- Webhook signature must be verified

**State Transitions:**
```
Order:         PENDING → PAID
               PENDING/PAID → CANCELLED
               PAID → cannot revert

Delivery:      PENDING → IN_TRANSIT → DELIVERED
               ANY → FAILED (terminal)

PaymentIntent: CREATED → CONFIRMED → (deleted after finalization)
               CREATED → EXPIRED
```

**Key Services:**
- `OrderService` — createIntent(), verifyPayment(), finalizeOrderAtomic(), cleanupIntent()
- `InventoryService` — softCheckStock(), hardDeductStock(), restoreStock()

---

## Domain 6: Payment (payment)

### Aggregate Root: Transaction
Manages payment methods, intents, transactions, and invoices.

**Entities:**
- `PaymentMethod` — card details (methodId, gatewayToken, cardType: VISA/MASTERCARD/AMEX, lastFour, expiryDate)
- `PaymentIntent` — payment session (intentId, transactionId, qrCodeData, status, expiresAt, methodId)
- `Transaction` — financial record (transactionId, amount, currency, payerType: USER/SHOP, payerId, planId, gatewayRef, status)
- `Invoice` — billing document (invoiceId, invoiceNo, transactionId, status, subTotal, taxAmount, discountPrice, totalAmount)
- `PlatformRevenueSnapshot` — daily platform revenue aggregate

**Invariants:**
- lastFour: exactly 4 digits
- amount ≥ 0 (BigDecimal, scale=2)
- gatewayRef: unique, immutable once set
- Invoice: subTotal + taxAmount - discountPrice == totalAmount (verifyTotal())
- paidAt set only on PAID transition

**State Transitions:**
```
Transaction:    PENDING → PAID (sets paidAt)
                PENDING → FAILED
                PAID/FAILED → terminal

PaymentIntent:  PENDING → SUCCESS / FAILED

Invoice:        ISSUED → CANCELLED
```

---

## Domain 7: Notification (notification)

### Aggregate Root: Notification
Manages in-app and email notification delivery.

**Entities:**
- `Notification` — notification record (notificationId, userId, type: TRANSACTIONAL/SYSTEM/SUPPORT, priority: HIGH/NORMAL, title, content, linkUrl, isRead)
- `NotificationPreferences` — user preferences (userId, emailEnabled, billingAlerts, marketingAlerts)
- `NotificationDispatchRecord` — dispatch audit (notificationId, channel: IN_APP/EMAIL, status, timestamps)
- `NotificationDispatchLog` — retry log

**Invariants:**
- Notifications are append-only (no delete)
- billingAlerts is always TRUE (immutable)
- Email dispatch only for HIGH priority AND emailEnabled = true
- In-app dispatch always available
- All queries scoped by userId

**Key Services:**
- `NotificationService` — createNotification(), markAsRead(), markAllAsRead(), getUnreadCount(), getUserNotifications()
- `PreferenceService` — getPreferences(), updatePreferences(), resetToDefault()
- `DispatchService` — dispatchNotification(), retryFailed(), scheduleDelivery()

---

## Domain 8: Feedback (feedback)

### Aggregate Root: FeatureSuggestion
Manages private user feedback to platform admins.

**Entities:**
- `FeatureSuggestion` — suggestion (suggestionId, userId, category, problemText, solutionText, status, adminNotes, createdAt)
- `SuggestionCategory` — enum (Inventory, Finance, Staff, UI, etc.)
- `SuggestionStatus` — enum (NEW, IN_REVIEW, BACKLOG, SHIPPED)

**Invariants:**
- Users can only edit their own suggestions while status is NEW
- Status transitions are forward-only
- Admin notes only writable by admins

**State Transitions:**
```
Suggestion: NEW → IN_REVIEW → BACKLOG → SHIPPED
```

**Key Services:**
- `FeedbackService` — submitSuggestion(), editSuggestion(), adminUpdateStatus(), listMySuggestions(), adminListByStatus()

---

## Domain 9: Reporting & Analytics (report-and-analytic)

### Aggregate Root: DailySnapshot
Immutable data warehouse snapshots for analytics.

**Entities:**
- `DailyProductSnapshot` — atomic level (snapshotId, snapshotDate, productId, variantId, sellerId, categoryId, shopId, qtySold, baseCostPrice, unitSalePrice, stockAtMidnight)
- `DailyCategorySnapshot` — middle level (snapshotId, snapshotDate, categoryId, shopId, catGrossRevenue, catNetProfit, catItemsSold)
- `DailySnapshot` — high level (snapshotId, snapshotDate, orgId, shopId, totalGross, totalProfit, orderCount)
- `StockAtMidnightFact` — inventory snapshot
- `OrderFact` / `OrderItemFact` — order read models

**Invariants:**
- All snapshots are write-once (no update/delete)
- One snapshot per shop per day
- All financial fields ≥ 0 (BigDecimal, scale=2)
- All snapshots scoped by shopId

**Key Services:**
- `AnalyticsAggregationService` — runDailyAggregation(), validateSnapshotUniqueness(), persistSnapshots()
- `ReportingService` — getDailyReport(), getWeeklyReport(), getMonthlyReport(), getAnnualReport()
- `SellerAnalyticsService` — rankSellers(), getSellerPerformance()
- `CategoryAnalyticsService` — getCategoryTrends(), comparePeriods()
- `ForecastService` — predictOutOfStock(), detectRevenueDrop()

---

## Domain 10: Revenue (revenue)

### Aggregate Root: PlatformRevenueSnapshot
Platform-level financial source of truth.

**Entities:**
- `PlatformRevenueSnapshot` — daily platform snapshot (platformSnapId, snapshotDate, totalSubscriptionRevenue, platformNetProfit, activePayingShops)

**Invariants:**
- snapshotDate is unique
- revenue ≥ 0, profit may be negative
- Snapshots are append-only
- Only DRAFT status can be recalculated

**State Transitions:**
```
Snapshot: DRAFT → FINALIZED → LOCKED
```

---

## Domain 11: Admin & User Settings (admin-userSetting)

Cross-cutting administrative domain managing platform governance.

**Concerns:**
- User account lifecycle administration
- Shop account lifecycle administration
- Platform Registry "God Mode" controls
- Session management and revocation
- Shop settings and user-shop configuration
- Data sync log monitoring
- Permission management

---

## Shared Infrastructure

| Component | Location | Purpose |
|---|---|---|
| `ValidationUtils` | core/utils | Centralized validation (UUID, money, slug, SKU, dates, regex) |
| `RegexPatterns` | core/utils | All regex constants |
| `DomainException` | common | Domain-level business rule violations |
| `ShopRepository` | core/repository | Shop persistence interface |
| `PostgresShopRepository` | infrastructure/db | PostgreSQL implementation |
| `DatabaseConfig` | infrastructure/util | DB connection configuration |
| `ConsoleUI` | infrastructure/ui | Console interface |

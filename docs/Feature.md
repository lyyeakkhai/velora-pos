# Velora Platform — Feature Catalog

## System Overview

Velora is a multi-tenant SaaS commerce platform built on Clean Architecture and Domain-Driven Design (DDD). It supports multi-shop vendor management, subscription billing, inventory control, sales processing, analytics, and platform governance.

---

## Feature List by Domain

### 1. Authentication & Identity Management (auth)
- User registration and profile management
- Email / Google / Facebook OAuth authentication
- Bcrypt password hashing and credential security
- Role-based access control (SUPER_ADMIN, OWNER, MANAGER, SELLER)
- Multi-shop membership with role assignments
- User lifecycle: ACTIVE → SUSPENDED → DELETED
- Seller identity management per shop

### 2. Plan & Subscription Management (plan_subscription)
- Subscription plan catalog (name, slug, price, duration, payer type)
- Feature catalog with per-plan feature flags and limits
- Platform Registry as master access switch (ACTIVE, BANNED, INACTIVE, PENDING)
- User account subscription lifecycle (ACTIVE, TRIAL, EXPIRED, CANCELLED)
- Shop account subscription lifecycle (ACTIVE, EXPIRED, CANCELLED, PAST_DUE)
- User subscription records with refund deadline tracking
- Shop subscription records with auto-renew support
- Plan activation, extension, upgrade, cancellation, renewal
- Expiration automation (cron/job support via markExpiredIfNeeded)
- Free trial onboarding (zero-price plan → TRIAL status)
- Registry decision engine routing activation to user or shop accounts

### 3. Store Management (store-management)
- Shop registration with PENDING → ACTIVE lifecycle
- Legal identity enforcement (legal_name + tax_id required for ACTIVE)
- Unique slug validation (URL-safe, lowercase)
- Cambodian province address validation
- Shop status transitions: PENDING, ACTIVE, SUSPENDED, BANNED
- BANNED shop recovery requires admin override
- Shop settings management
- Shop account linking to subscription registry
- Payout calculation support

### 4. Inventory & Event Management (inventory-event-management)
- Product catalog with name, slug, base price, cost price, category
- Product variant management (size, color, SKU, stock quantity)
- Category management with ownership validation
- Event types (discount rules: PERCENTAGE, FIXED)
- Event product junction with status lifecycle: SCHEDULED → ACTIVE → ENDED
- Discount engine with profit margin protection (finalPrice > costPrice)
- Role enforcement on all mutations (OWNER full, MANAGER limited, SELLER read-only)
- Atomic product creation (product + variants in one transaction)
- Row-level access security (all queries scoped by shopId)
- Soft delete support (deletedAt)

### 5. Sale Management (sale-management)
- Payment intent creation from immutable cart snapshot
- Webhook signature verification and replay attack prevention
- Atomic order finalization (lock intent → create order → deduct inventory → issue receipt → create delivery → commit)
- Order lifecycle: PENDING → PAID → CANCELLED
- Order item subtotal calculation and total verification
- Receipt generation with INV-XXXX format
- Delivery lifecycle: PENDING → IN_TRANSIT → DELIVERED / FAILED
- Inventory soft check (pre-payment) and hard deduct (post-payment)
- Expired intent cleanup and unpaid order cancellation
- Payment reconciliation (idempotent webhook replay)

### 6. Payment & Revenue Tracking (payment)
- Payment method management (VISA, MASTERCARD, AMEX with last-four digits)
- Payment intent with QR code support and expiry
- Transaction lifecycle: PENDING → PAID / FAILED
- Invoice generation with sub-total, tax, discount, and total verification
- Invoice cancellation
- Platform revenue snapshots (daily aggregated subscription revenue)
- BigDecimal monetary accuracy (scale=2, HALF_UP) throughout

### 7. Notification System (notification)
- In-app notification creation (append-only, no delete)
- Email dispatch for HIGH priority notifications only
- User notification preferences (email enabled, billing alerts, marketing alerts)
- Billing alerts are always forced ON (immutable)
- Notification lifecycle: created → dispatched → read
- Dispatch logging with retry support
- Role-scoped access: users read own inbox, ADMIN sends system notifications only
- Paginated notification queries (cursor-based, newest-first)
- Unread count tracking

### 8. Feedback & Suggestions (feedback)
- Private feature suggestion submission by users (OWNER/STAFF)
- Suggestion categories: Inventory, Finance, Staff, UI, etc.
- Suggestion lifecycle: NEW → IN_REVIEW → BACKLOG → SHIPPED
- Owner can edit suggestions while status is NEW
- Admin status management with admin notes
- Role-based access: users see own suggestions, admins see all

### 9. Reporting & Analytics (report-and-analytic)
- Daily product snapshots (qty sold, sale price, stock at midnight)
- Daily category snapshots (gross revenue, net profit, items sold)
- Daily shop-level snapshots (total gross, total profit, order count)
- Scheduled daily aggregation job (runs at 00:05 UTC, idempotent)
- Weekly, monthly, and annual reporting from snapshots only
- Seller performance ranking and analytics
- Category trend comparison across periods
- Out-of-stock prediction and revenue drop detection
- AI-ready AnalyticsInsightDTO with change percent and risk level
- Row-level access security (all snapshots scoped by shopId)
- Role-scoped access: OWNER full, MANAGER category/seller, SELLER personal only

### 10. Revenue Tracking (revenue)
- Platform-level daily revenue snapshots
- Total subscription revenue aggregation
- Platform net profit calculation (revenue - infrastructure costs)
- Active paying shops count
- Snapshot status lifecycle: DRAFT → FINALIZED → LOCKED
- Scheduled revenue aggregation job (runs at 00:10 UTC)
- Read-only for non-admin users
- Audit compliance with hash checksum and regeneration history

### 11. Admin & User Settings (admin-userSetting)
- User account lifecycle management
- Shop account lifecycle management
- Platform Registry "God Mode" admin controls
- Session management and revocation
- Shop settings and user-shop configuration
- Data sync log monitoring
- Permission management
- Admin banning and suspension workflows
- Platform revenue snapshot access

---

## Cross-Cutting Concerns

- Centralized validation via `ValidationUtils` (UUID, money, slug, SKU, dates, regex)
- Regex patterns centralized in `RegexPatterns`
- `DomainException` for domain-level business rule violations
- No default constructors — all entities require mandatory fields
- Immutable IDs and audit timestamps (private setters)
- BigDecimal for all monetary values (scale=2, HALF_UP)
- Enums for all status and type fields
- Row-level access security (shopId scoping) across inventory, analytics, and sales
- Clean Architecture: no SQL in domain entities, repository abstraction

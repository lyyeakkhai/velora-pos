# Velora POS - Feature Documentation Overview

## System Architecture

Velora is a multi-tenant SaaS commerce platform built on Clean Architecture and Domain-Driven Design (DDD). The system supports multi-shop vendor management, subscription billing, inventory control, sales processing, analytics, and platform governance.

```
┌─────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                             │
│  Entities · Value Objects · Domain Services · Repositories │
├─────────────────────────────────────────────────────────────┤
│                  APPLICATION LAYER                          │
│         Use Cases · Orchestration · DTOs                    │
├─────────────────────────────────────────────────────────────┤
│                 INFRASTRUCTURE LAYER                        │
│    PostgreSQL · Email Gateway · Console UI · Config           │
└─────────────────────────────────────────────────────────────┘
```

## Feature Summary

| # | Feature | Module | Description |
|---|---------|--------|-----------|
| 1 | Authentication & Identity Management | authModule | User registration, OAuth, password security, role-based access control |
| 2 | Plan & Subscription Management | plan_subscriptionModule | Subscription plans, feature catalog, platform registry, billing lifecycle |
| 3 | Store Management | store-managementModule | Shop registration, legal identity, status lifecycle, payout calculation |
| 4 | Inventory & Event Management | inventory-managementModule | Products, variants, categories, discount events, stock control |
| 5 | Sale Management | sale-managementModule | Payment intents, orders, receipts, delivery lifecycle |
| 6 | Payment | paymentModule | Payment methods, transactions, invoices, revenue snapshots |
| 7 | Notification | notificationModule | In-app notifications, email dispatch, user preferences |
| 8 | Feedback | feedback | Feature suggestions, status workflow, admin management |
| 9 | Reporting & Analytics | report_analyticModule | Daily snapshots, aggregation jobs, seller rankings, AI insights |
| 10 | Revenue | revenueModule | Platform revenue snapshots, profit tracking, audit compliance |
| 11 | Admin & User Settings | admin | User lifecycle, shop lifecycle, platform registry, session management |

## Implementation Status

| Feature | Status | OOP Maturity |
|---------|--------|-------------|
| 1. Authentication | Complete | **Full OOP** - Reference implementation |
| 2. Plan & Subscription | Design Only | Low - Needs implementation |
| 3. Store Management | Design Only | Low - Needs implementation |
| 4. Inventory & Event Management | Design Only | Low - Needs implementation |
| 5. Sale Management | Design Only | Low - Needs implementation |
| 6. Payment | Design Only | Low - Needs implementation |
| 7. Notification | Design Only | Low - Needs implementation |
| 8. Feedback | Design Only | Low - Needs implementation |
| 9. Reporting & Analytics | Design Only | Low - Needs implementation |
| 10. Revenue | Design Only | Low - Needs implementation |
| 11. Admin & User Settings | Design Only | Low - Needs implementation |

## Dependencies Between Features

```
Authentication (1)
    │
    ├──► Plan & Subscription (2) ──► Store Management (3)
    │         │                              │
    │         ▼                              ▼
    │    Payment (6) ◄─────── Inventory & Event (4) ◄── Sale Management (5)
    │         │                                            │
    │         ▼                                            ▼
    │    Revenue (10)                                  Delivery
    │         │                                            │
    ├──► Notification (7) ◄──────────────────────────────┘
    │         │
    ├──► Feedback (8)
    │         │
    └──► Admin & User Settings (11)
              │
              ▼
       Reporting & Analytics (9)
```

## OOP Best Practices Applied

### Authentication (Reference Implementation)

The Authentication module serves as the reference example for OOP implementation:

1. **Setter Validation in Constructors** - All validations occur in constructors and setters with fail-fast approach
2. **Immutability** - IDs are final after creation, timestamps managed internally
3. **Specific Exceptions** - DomainException usage, though currently a generic wrapper
4. **No Default Constructors** - All entities require mandatory fields
5. **AbstractAuditableEntity** - Base class providing UUID and audit timestamps

### Validation Patterns Used

- `ValidationUtils.validateNotBlank()` - String validation
- `ValidationUtils.validateUUID()` - UUID format validation
- `ValidationUtils.validateFormat()` - Regex pattern validation
- `ValidationUtils.validateBcryptHash()` - Password hash validation

### Rules for New Feature Implementation

All features should follow the Authentication module pattern:

1. Validate in constructors using private setters
2. No default constructors
3. Immutable IDs and audit timestamps
4. Use enums for all status/type fields
5. BigDecimal for monetary values (scale=2, HALF_UP)
6. Row-level access security (shopId scoping)
7. No raw strings for state management

## Known Limitations

1. **DomainException is generic** - True OOP requires specific exception subclasses (e.g., `InvalidEmailException`, `InvalidPasswordException`)
2. **Features 2-11 lack implementation** - Only design documentation exists
3. **No test files for all modules** - Need comprehensive testing

## Cross-Cutting Concerns

- Centralized validation via `ValidationUtils`
- Regex patterns centralized in `RegexPatterns`
- `DomainException` for domain-level business rule violations
- No default constructors
- Immutable IDs and audit timestamps
- BigDecimal for all monetary values
- Enums for all status and type fields
- Row-level access security

## Directory Structure

```
docs/feature/
├── OVERVIEW.md              # This file
├── 01-authentication.md    # Feature 1: Authentication
├── 02-plan-subscription.md # Feature 2: Plan & Subscription
├── 03-store-management.md  # Feature 3: Store Management
├── 04-inventory-event-management.md # Feature 4
├── 05-sale-management.md  # Feature 5: Sale Management
├── 06-payment.md           # Feature 6: Payment
├── 07-notification.md      # Feature 7: Notification
├── 08-feedback.md          # Feature 8: Feedback
├── 09-report-analytic.md   # Feature 9: Reporting & Analytics
├── 10-revenue.md           # Feature 10: Revenue
└── 11-admin-userSetting.md # Feature 11: Admin & User Settings
```

---

**Last Updated**: 2026-04-06  
**Version**: 1.0
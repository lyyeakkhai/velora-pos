# Feature 2: Plan & Subscription Management

## System Overview

This module controls feature access, billing lifecycle, and account activation for both Users and Shops. It implements a subscription engine with plan management, feature catalogs, and platform registry as the master access switch.

## Domain Model

### Entities

#### SubscriptionPlan
Plan catalog with pricing and duration.

| Field | Type | Validation |
|-------|------|------------|
| planId | UUID | Immutable |
| name | String | Not blank |
| slug | String | Unique, lowercase, URL-safe |
| price | BigDecimal | >= 0, scale=2, HALF_UP |
| durationMonths | int | > 0 |
| payerType | PayerType | USER/SHOP |
| isActive | boolean | Default true |

#### Feature
Feature catalog with target type.

| Field | Type | Validation |
|-------|------|------------|
| featureId | UUID | PK |
| featureKey | String | Unique, valid identifier |
| targetType | TargetType | USER/SHOP/BOTH |
| description | String | Not blank |

#### PlanFeature
Bridge table between plans and features.

| Field | Type | Validation |
|-------|------|------------|
| planId | UUID | FK |
| featureId | UUID | FK |
| limitValue | Integer | Nullable |
| isEnabled | boolean | Default true |

#### PlatformRegistry
Master access switch for the platform.

| Field | Type | Validation |
|-------|------|------------|
| registryId | UUID | Immutable |
| ownerId | UUID | FK to User/Shop |
| targetType | TargetType | USER/SHOP |
| status | RegistryStatus | ACTIVE/BANNED/INACTIVE/PENDING |
| banReason | String | Nullable |
| transactionId | UUID | Nullable |

#### UserAccount
User subscription lifecycle.

| Field | Type | Validation |
|-------|------|------------|
| subscriptionId | UUID | PK |
| userId | UUID | FK |
| planId | UUID | FK |
| registryId | UUID | FK |
| status | AccountStatus | ACTIVE/TRIAL/EXPIRED/CANCELLED |
| startDate | LocalDateTime | Managed internally |
| endDate | LocalDateTime | Managed internally |
| refundDeadline | LocalDateTime | startDate + 14 days |

#### ShopAccount
Shop subscription lifecycle.

| Field | Type | Validation |
|-------|------|------------|
| subscriptionId | UUID | PK |
| shopId | UUID | FK |
| planId | UUID | FK |
| registryId | UUID | FK |
| status | AccountStatus | ACTIVE/EXPIRED/CANCELLED/PAST_DUE |
| startDate | LocalDateTime | Managed |
| endDate | LocalDateTime | Managed |
| refundDeadline | LocalDateTime | Managed |
| isAutoRenew | boolean | Default false |

#### UserSubscription
User subscription record.

| Field | Type | Validation |
|-------|------|------------|
| subscriptionId | UUID | PK |
| userId | UUID | FK |
| transactionId | UUID | FK |
| planId | UUID | FK |
| status | SubscriptionStatus | ACTIVE/EXPIRED/CANCELLED/REFUNDED |
| startDate | LocalDateTime | Managed |
| endDate | LocalDateTime | Managed |
| refundDeadline | LocalDateTime | Managed |

#### ShopSubscription
Shop subscription record.

| Field | Type | Validation |
|-------|------|------------|
| subscriptionId | UUID | PK |
| shopId | UUID | FK |
| transactionId | UUID | FK |
| planId | UUID | FK |
| status | SubscriptionStatus | ACTIVE/EXPIRED/PAST_DUE/REFUNDED |
| startDate | LocalDateTime | Managed |
| endDate | LocalDateTime | Managed |
| refundDeadline | LocalDateTime | Managed |

### Value Objects

| Value Object | Description |
|--------------|------------|
| PayerType | USER, SHOP |
| TargetType | USER, SHOP, BOTH |
| RegistryStatus | ACTIVE, BANNED, INACTIVE, PENDING |
| AccountStatus | ACTIVE, TRIAL, EXPIRED, CANCELLED, PAST_DUE |
| SubscriptionStatus | ACTIVE, EXPIRED, CANCELLED, REFUNDED, PAST_DUE |

## Entity Relationships

```
SubscriptionPlan (1:N) PlanFeature (N:1) Feature
PlatformRegistry (1:N) UserAccount
PlatformRegistry (1:N) ShopAccount
UserAccount (1:N) UserSubscription
ShopAccount (1:N) ShopSubscription
```

## Business Rules

1. Price must be >= 0 with BigDecimal scale=2, HALF_UP
2. Duration months must be > 0
3. Slug must be lowercase, unique, URL-safe
4. Feature key must be valid identifier format
5. Start date < end date
6. Refund deadline = start date + 14 days
7. BANNED registry cannot return to ACTIVE without admin override

## State Transitions

```
PlatformRegistry: PENDING → ACTIVE → INACTIVE
                ANY → BANNED (terminal without admin)

UserAccount:     TRIAL/ACTIVE → EXPIRED → CANCELLED
ShopAccount:    ACTIVE → EXPIRED → CANCELLED/PAST_DUE
Subscription:  ACTIVE → EXPIRED → CANCELLED/REFUNDED
```

## Implementation Details

### Business Methods

| Entity | Methods |
|--------|---------|
| PlatformRegistry | activate(), ban(reason), deactivate(), verifyAccess() |
| SubscriptionPlan | isAvailable(), disable(), enable() |
| UserAccount/ShopAccount | activatePlan(), extendPlan(), expire(), cancel(), renew(), isActive(), upgrade(plan) |
| Subscription | calculateEndDate(), calculateRefundDeadline(), markRefunded() |
| Feature | hasFeature(featureKey), getFeatureLimit(featureKey) |

### Lifecycle Automation

- checkExpiration() - Verify if subscription expired
- markExpiredIfNeeded() - Auto-expire based on endDate

### Registry Decision Engine

Routes activation to UserAccount or ShopAccount based on targetType.

## OOP Best Practices Applied

1. **Constructor Validation** - All validation in constructors/setters
2. **Immutability** - IDs and created timestamps immutable
3. **No Default Constructors** - Required fields mandatory
4. **Enums** - All status fields use enums
5. **State Machine Pattern** - Entity state transitions controlled by business methods
6. **Domain-Driven Design** - Clear separation between entities, repositories, and services
7. **Exception Handling** - Uses DomainException, IllegalStateException, and IllegalArgumentException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `plan_subscriptionModule/domain/SubscriptionPlan.java` | Plan catalog entity |
| `plan_subscriptionModule/domain/Feature.java` | Feature catalog entity |
| `plan_subscriptionModule/domain/PlanFeature.java` | Plan-feature bridge entity |
| `plan_subscriptionModule/domain/PlatformRegistry.java` | Master access switch entity |
| `plan_subscriptionModule/domain/UserAccount.java` | User subscription lifecycle |
| `plan_subscriptionModule/domain/ShopAccount.java` | Shop subscription lifecycle |
| `plan_subscriptionModule/domain/UserSubscription.java` | User subscription record |
| `plan_subscriptionModule/domain/ShopSubscription.java` | Shop subscription record |
| `plan_subscriptionModule/domain/PlanSubscriptionEngine.java` | Domain logic for onboarding |
| `plan_subscriptionModule/domain/SubscriptionActivationRouter.java` | Routes activation to user/shop |
| `common/DomainException.java` | Base business exception |

### Exception Handling

The module uses three types of exceptions:

| Exception Type | Usage |
|---------------|-------|
| `DomainException` | General business rule violations (via SubscriptionActivationRouter) |
| `IllegalStateException` | State transition violations (account status, registry status) |
| `IllegalArgumentException` | Parameter validation failures (plan type mismatch, null checks) |

```
RuntimeException
└── DomainException (common)
    ├── IllegalStateException (state transitions)
    └── IllegalArgumentException (validation)
```

## Known Limitations

1. **No specific exception classes** - Uses generic RuntimeException types instead of OOP-specific subclasses
2. **No test files** - Need comprehensive testing

## Dependencies

- **Depends on**: Feature 1 (Authentication)
- **Used by**: Feature 3 (Store Management), Feature 6 (Payment)

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: Medium - Full entity implementation with state machine pattern  
**Last Updated**: 2026-04-07
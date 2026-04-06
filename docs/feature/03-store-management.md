# Feature 3: Store Management

## System Overview

This feature manages the lifecycle, identity, and business rules of vendor shops. It handles shop registration, legal identity enforcement, status management, and payout calculations for multi-tenant e-commerce.

## Domain Model

### Entities

#### Shop
Aggregate root managing vendor shop lifecycle.

| Field | Type | Validation |
|-------|------|------------|
| shopId | UUID | Immutable |
| ownerId | UUID | FK to User |
| legalName | String | Required for ACTIVE status |
| taxId | String | Required for ACTIVE status |
| slug | String | Unique, lowercase, URL-safe |
| status | ShopStatus | PENDING/ACTIVE/SUSPENDED/BANNED |
| street | String | Street address |
| city | String | City |
| district | String | District |
| province | Province | Cambodian provinces only |
| createdAt | LocalDateTime | Immutable |

#### Address (Value Object)
Physical address with Cambodian provinces.

| Field | Type | Validation |
|-------|------|------------|
| street | String | Not blank |
| city | String | Not blank |
| district | String | Not blank |
| province | Province | Must be valid Cambodian province |

#### ShopAccount
Subscription reference linking to platform registry.

| Field | Type | Validation |
|-------|------|------------|
| subscriptionId | UUID | PK |
| shopId | UUID | FK |
| registryId | UUID | FK to PlatformRegistry |
| status | AccountStatus | Linked to subscription |

#### ShopSettings
Shop configuration settings.

| Field | Type | Validation |
|-------|------|------------|
| settingsId | UUID | PK |
| shopId | UUID | FK |
| currency | Currency | Default USD |
| timezone | String | Default Asia/Phnom_Penh |
| isActive | boolean | Default true |

### Value Objects

| Value Object | Description |
|--------------|------------|
| ShopStatus | PENDING, ACTIVE, SUSPENDED, BANNED |
| Province | Cambodian provinces (23 provinces) |

## Entity Relationships

```
User (1:N) Shop
Shop (1:1) ShopAccount
Shop (1:1) ShopSettings
Shop (1:N) Membership (via shopId)
```

## Business Rules

1. Shop cannot be ACTIVE without tax_id and legal_name
2. Slug must be unique (globally unique)
3. Slug must be lowercase, URL-safe
4. BANNED shops cannot revert to ACTIVE without admin override
5. Province must match official Cambodian provinces
6. All entities scoped by shopId (row-level security)

## State Transitions

```
Shop: PENDING → ACTIVE → SUSPENDED → BANNED
      BANNED → ACTIVE (admin override only)
```

## Implementation Details

### Service Logic

| Service | Methods |
|---------|---------|
| StoreManagementService | registerShop(), updateShopStatus(), payoutCalculation() |

### Shop Registration Flow

1. Create User subscription (if not exists)
2. Create PlatformRegistry (PENDING)
3. Create Shop (PENDING)
4. Link ShopAccount to registry

### Status Update Flow

1. Validate role (OWNER or ADMIN)
2. Check business rules per status
3. Update status
4. Trigger notification if needed

### Payout Calculation

Based on shop balance minus platform fees.

## OOP Best Practices Applied

1. **Constructor Validation** - All validation in constructors/setters
2. **Immutability** - IDs and created timestamps immutable
3. **No Default Constructors** - Required fields mandatory
4. **Enums** - All status fields use enums
5. **Value Objects** - Address as embedded value object
6. **State Machine Pattern** - Shop status transitions controlled by business methods
7. **Exception Handling** - Uses IllegalStateException and IllegalArgumentException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `store/domain/Shop.java` | Aggregate root for shop lifecycle |
| `store/domain/Address.java` | Value object for physical address |
| `store/domain/ShopAccount.java` | Subscription reference entity |
| `store/domain/ShopSettings.java` | Shop configuration entity |
| `common/DomainException.java` | Base business exception |

### Exception Handling

The module uses two types of exceptions:

| Exception Type | Usage |
|---------------|-------|
| `IllegalStateException` | State transition violations (shop status, admin override rules) |
| `IllegalArgumentException` | Parameter validation failures (address fields, null checks) |

```
RuntimeException
├── IllegalStateException (state transitions)
│   ├── "Shop must be verified (legalName + taxId) before activation"
│   ├── "Banned shops require admin override to activate"
│   ├── "Cannot suspend a pending shop"
│   ├── "Shop is already banned"
│   └── "Only banned shops can be reactivated"
└── IllegalArgumentException (validation)
    ├── "Street cannot be null or blank"
    ├── "City cannot be null or blank"
    └── "Province cannot be null"
```

## Known Limitations

1. **No specific exception classes** - Uses generic RuntimeException types
2. **No payout processing** - Calculation only, no actual payouts

## Dependencies

- **Depends on**: Feature 1 (Authentication), Feature 2 (Plan & Subscription)
- **Used by**: Feature 4 (Inventory), Feature 5 (Sale Management)

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: Medium - Full entity implementation with state machine pattern  
**Last Updated**: 2026-04-07
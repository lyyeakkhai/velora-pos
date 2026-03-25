# Velora POS - Project Structure

## Overview

Velora is a multi-tenant SaaS commerce platform built on Clean Architecture and Domain-Driven Design (DDD). It supports multi-shop vendor management, subscription billing, inventory control, sales processing, analytics, and platform governance.

---

## Directory Layout

```
velora_pos/
├── pom.xml                          # Maven configuration with JUnit 5, BCrypt
├── docs/                            # Architecture and feature documentation
├── src/
│   ├── main/
│   │   └── java/com/velora/app/
│   │       │
│   │       ├── core/                # Shared utilities (no SQL/UI logic)
│   │       │   └── utils/
│   │       │       ├── ValidationUtils.java    # Centralized validation
│   │       │       └── RegexPatterns.java     # Regex constants
│   │       │
│   │       ├── common/              # Shared base classes (OOP patterns)
│   │       │   ├── AbstractEntity.java         # UUID + equals/hashCode
│   │       │   ├── AbstractAuditableEntity.java # + createdAt/updatedAt
│   │       │   ├── AbstractDomainService.java   # Base for services
│   │       │   ├── DomainException.java        # Business rule exceptions
│   │       │   ├── AbstractDiscountCalculator.java
│   │       │   ├── AbstractAccessPolicy.java
│   │       │   ├── AbstractNotificationDispatcher.java
│   │       │   ├── AbstractReportPeriod.java
│   │       │   ├── AbstractSnapshot.java
│   │       │   ├── AbstractSnapshotAggregator.java
│   │       │   ├── AbstractSubscriptionAccount.java
│   │       │   └── AbstractSubscriptionRecord.java
│   │       │
│   │       ├── infrastructure/      # Technical implementations
│   │       │   ├── db/             # PostgreSQL JDBC repositories
│   │       │   │   ├── PostgresUserRepository.java
│   │       │   │   ├── PostgresShopRepository.java
│   │       │   │   └── ...
│   │       │   ├── ui/             # Console UI (entry point)
│   │       │   │   └── ConsoleUI.java
│   │       │   ├── security/       # Security implementations
│   │       │   │   └── BCryptPasswordEncoder.java
│   │       │   └── util/
│   │       │       └── DatabaseConfig.java
│   │       │
│   │       └── modules/             # Domain modules (DDD)
│   │           ├── auth/           # Authentication & Identity
│   │           │   ├── domain/
│   │           │   │   ├── User.java
│   │           │   │   ├── UserAuth.java
│   │           │   │   ├── Membership.java
│   │           │   │   ├── Role.java
│   │           │   │   └── PasswordEncoder.java (interface)
│   │           │   ├── repository/
│   │           │   │   ├── UserRepository.java
│   │           │   │   ├── UserAuthRepository.java
│   │           │   │   └── MembershipRepository.java
│   │           │   └── service/
│   │           │       ├── AuthService.java
│   │           │       └── IAuthService.java
│   │           │
│   │           ├── store/           # Store Management
│   │           │   └── domain/
│   │           │       ├── Shop.java          # Aggregate Root
│   │           │       └── Address.java       # Value Object
│   │           │
│   │           ├── plan-subscription/  # Subscription Billing
│   │           │   └── domain/
│   │           │       ├── SubscriptionPlan.java
│   │           │       ├── Feature.java
│   │           │       ├── PlanFeature.java
│   │           │       ├── PlatformRegistry.java
│   │           │       ├── UserAccount.java
│   │           │       ├── ShopAccount.java
│   │           │       ├── UserSubscription.java
│   │           │       └── ShopSubscription.java
│   │           │
│   │           ├── inventory/        # Inventory Management
│   │           │   └── domain/
│   │           │       ├── Product.java
│   │           │       ├── ProductVariant.java
│   │           │       ├── Category.java
│   │           │       ├── EventType.java
│   │           │       └── EventProduct.java
│   │           │
│   │           ├── sale/            # Sale Management
│   │           │   └── domain/
│   │           │       ├── Order.java            # Aggregate Root
│   │           │       ├── OrderItem.java
│   │           │       ├── Receipt.java
│   │           │       ├── Delivery.java
│   │           │       └── PaymentIntent.java
│   │           │
│   │           ├── payment/         # Payment Processing
│   │           │   └── domain/
│   │           │       ├── Transaction.java
│   │           │       ├── Invoice.java
│   │           │       ├── PaymentMethod.java
│   │           │       └── PlatformRevenueSnapshot.java
│   │           │
│   │           ├── notification/     # Notification System
│   │           │   └── domain/
│   │           │       ├── Notification.java
│   │           │       ├── NotificationPreferences.java
│   │           │       └── NotificationDispatchRecord.java
│   │           │
│   │           ├── feedback/        # User Feedback
│   │           │   └── domain/
│   │           │       └── FeatureSuggestion.java
│   │           │
│   │           ├── analytics/       # Reporting & Analytics
│   │           │   └── domain/
│   │           │       └── DailySnapshot.java
│   │           │
│   │           └── admin/           # Admin Operations
│   │               └── service/
│   │                   └── AdminService.java
│   │
│   └── test/
│       └── java/com/velora/app/     # Unit tests (JUnit 5)
│           └── modules/
│               └── auth/
│                   └── AuthServiceTest.java
│
└── resources/                      # Configuration files
    └── application.properties
```

---

## Domain Modules

### 1. Auth Module (`auth`)
- **Purpose:** User registration, login, role-based access control
- **Entities:** User, UserAuth, Membership, Role
- **Key Feature:** Multi-shop membership with role assignments

### 2. Store Module (`store`)
- **Purpose:** Shop registration and management
- **Entities:** Shop (aggregate), Address (value object)
- **Key Feature:** Legal identity enforcement for shop activation

### 3. Plan-Subscription Module (`plan-subscription`)
- **Purpose:** Subscription billing and feature access control
- **Entities:** SubscriptionPlan, Feature, PlatformRegistry, UserAccount, ShopAccount
- **Key Feature:** Feature flags and plan limits per tenant

### 4. Inventory Module (`inventory`)
- **Purpose:** Product catalog and discount management
- **Entities:** Product, ProductVariant, Category, EventType, EventProduct
- **Key Feature:** Atomic product creation with variants

### 5. Sale Module (`sale`)
- **Purpose:** Order processing and delivery
- **Entities:** Order (aggregate), OrderItem, Receipt, Delivery, PaymentIntent
- **Key Feature:** Atomic order finalization with inventory deduction

### 6. Payment Module (`payment`)
- **Purpose:** Financial transactions and revenue tracking
- **Entities:** Transaction, Invoice, PaymentMethod, PlatformRevenueSnapshot
- **Key Feature:** Invoice verification and daily revenue aggregation

### 7. Notification Module (`notification`)
- **Purpose:** In-app and email notifications
- **Entities:** Notification, NotificationPreferences, NotificationDispatchRecord
- **Key Feature:** User preference management with billing alerts

### 8. Feedback Module (`feedback`)
- **Purpose:** Feature suggestions from users
- **Entities:** FeatureSuggestion
- **Key Feature:** Status lifecycle for suggestion tracking

### 9. Analytics Module (`analytics`)
- **Purpose:** Reporting and business intelligence
- **Entities:** DailySnapshot
- **Key Feature:** Immutable snapshots for historical analysis

### 10. Admin Module (`admin`)
- **Purpose:** Platform governance operations
- **Services:** AdminService
- **Key Feature:** User/shop lifecycle management, banning, suspension

---

## OOP Design Patterns Applied

### 1. Inheritance
- **AbstractEntity:** Base class with UUID identity
- **AbstractAuditableEntity:** Adds createdAt/updatedAt timestamps
- **AbstractSubscriptionAccount:** Shared fields for UserAccount/ShopAccount
- **AbstractSubscriptionRecord:** Shared fields for UserSubscription/ShopSubscription
- **AbstractSnapshot:** Immutable snapshot base

### 2. Abstraction
- **Repository interfaces:** Define contracts without implementation
- **Service interfaces:** Define business operations
- **PasswordEncoder interface:** Allows algorithm switching

### 3. Encapsulation
- Private fields with public getters
- Validation in setters and constructors
- No public setters for immutable fields

### 4. Polymorphism
- **Role-based access:** Different behaviors per role
- **State machine:** Status transitions via methods
- **Discount strategies:** Variable discount calculations

---

## State Machines

### Shop Status
```
PENDING → ACTIVE → SUSPENDED → BANNED
BANNED → ACTIVE (admin override only)
```

### Order Status
```
PENDING → PAID → CANCELLED
PAID → cannot revert
```

### Transaction Status
```
PENDING → PAID / FAILED (terminal)
```

### Notification Status
```
Created → Dispatched → Read
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Build Tool | Maven |
| Testing | JUnit 5 |
| Database | PostgreSQL (via JDBC) |
| Password Hashing | BCrypt |
| Architecture | Clean Architecture + DDD |

---

## Best Practices

1. **No default constructors** - All entities require mandatory fields
2. **Validation in constructors/setters** - Fail-fast approach
3. **BigDecimal for monetary values** - Scale=2, HALF_UP rounding
4. **UUID for identifiers** - Distributed system friendly
5. **Enums for status fields** - Type-safe state management
6. **Immutable snapshots** - Analytics data never changes
7. **Row-level security** - All queries scoped by shopId
8. **No SQL in domain** - Repository abstraction

---

## Next Steps

1. Implement repository interfaces in `infrastructure/db/`
2. Add unit tests with JUnit 5
3. Create database schema in `resources/schema.sql`
4. Implement DI wiring in Main.java
5. Add service orchestration layers

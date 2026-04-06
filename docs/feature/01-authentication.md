# Feature 1: Authentication & Identity Management

## System Overview

This module implements Authentication, Authorization, and User Identity Management for the Velora POS system. The design follows Clean Architecture and Domain-Driven Design principles, ensuring separation of concerns and maintainability.

## Domain Model

### Entities

#### User
Main user profile with lifecycle management.

| Field | Type | Validation |
|-------|------|------------|
| userId | UUID | Immutable, generated at creation |
| username | String | 3-30 chars, alphanumeric + underscore |
| profileUrl | String | Nullable, URL format |
| bio | String | Nullable, max 500 chars |
| status | UserStatus | ACTIVE/SUSPENDED/DELETED |

#### UserAuth
Authentication credentials and provider management.

| Field | Type | Validation |
|-------|------|------------|
| authId | UUID | Immutable |
| userId | UUID | FK to User |
| email | String | RFC5322 format |
| passwordHash | String | bcrypt >=60 chars |
| provider | AuthProvider | EMAIL/GOOGLE/FACEBOOK |
| providerUid | String | Nullable, unique per provider |

#### Membership
User-to-shop access control and role assignments.

| Field | Type | Validation |
|-------|------|------------|
| membershipId | UUID | Immutable |
| userId | UUID | FK to User |
| shopId | UUID | FK to Shop |
| roleId | UUID | FK to Role |
| sellerName | String | Seller identity per shop |
| joinedAt | LocalDateTime | Immutable |

#### Role
Permission classification system.

| Field | Type | Validation |
|-------|------|------------|
| roleId | UUID | Immutable |
| roleName | RoleName | SUPER_ADMIN/OWNER/MANAGER/SELLER |

### Value Objects

| Value Object | Description |
|--------------|-------------|
| Provider | Auth provider enum (EMAIL, GOOGLE, FACEBOOK) |
| RoleName | Permission enum (SUPER_ADMIN, OWNER, MANAGER, SELLER) |
| UserStatus | Lifecycle enum (ACTIVE, SUSPENDED, DELETED) |

## Entity Relationships

```
User (1:1) UserAuth
    - Every user has exactly one authentication record
    - UserAuth contains login credentials and provider info

User (1:N) Membership
    - Users can belong to multiple shops with different roles
    - Each membership defines access level in a specific shop

Role (1:N) Membership
    - Predefined roles determine permission levels
    - Reusable across memberships
```

## Business Rules

1. Username must be 3-30 characters, alphanumeric + underscore
2. Email must be valid RFC5322 format
3. Password must be bcrypt hash with minimum 60 characters
4. Membership timestamps cannot be in the future
5. Role must be a valid enum value

## State Transitions

```
User: ACTIVE → SUSPENDED → DELETED
```

## Implementation Details

### Validation Strategy

- All validations occur in constructors and setters
- Fail-fast approach with meaningful exceptions
- No partial object creation allowed
- Private validation methods for each constraint

### Immutability Approach

- IDs are final after creation
- Timestamps managed internally
- State changes only through validated setters

### Security Design

- Passwords must be pre-hashed (bcrypt recommended)
- System never stores raw passwords
- Hash length validation ensures proper encryption
- Multiple authentication provider support
- Provider UID for external auth systems
- Role-based permissions through memberships

## OOP Best Practices Applied

1. **Setter Validation in Constructors** - Validation occurs in private setters called from constructors
2. **Immutability** - IDs are final after creation
3. **No Default Constructors** - All entities require mandatory fields
4. **AbstractAuditableEntity** - Base class providing UUID and audit timestamps
5. **DomainException** - Generic exception for business rule violations
6. **Specific Exception Subclasses** - Each failure scenario has dedicated exception class
7. **Exception Inheritance Chain** - Clear hierarchy from DomainException → AuthException → Specific exceptions

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `authModule/domain/User.java` | User entity |
| `authModule/domain/UserAuth.java` | Authentication credentials |
| `authModule/domain/Membership.java` | Shop access control |
| `authModule/domain/Role.java` | Permission classification |
| `common/DomainException.java` | Base business exception |
| `authModule/exception/AuthException.java` | Auth-specific exception base class |
| `authModule/exception/InvalidCredentialsException.java` | Login failure exception |
| `authModule/exception/UserNotFoundException.java` | User lookup failure exception |
| `authModule/exception/EmailAlreadyExistsException.java` | Duplicate email registration exception |
| `authModule/exception/UsernameAlreadyTakenException.java` | Duplicate username exception |
| `authModule/exception/MembershipNotFoundException.java` | Membership lookup failure exception |
| `authModule/exception/UnsupportedOAuthProviderException.java` | OAuth provider not supported exception |
| `authModule/exception/UnauthorizedRoleAssignmentException.java` | Role permission violation exception |
| `core/utils/ValidationUtils.java` | Validation utilities |

### Exception Hierarchy

```
DomainException (common)
└── AuthException (authModule)
    ├── InvalidCredentialsException
    ├── UserNotFoundException
    ├── EmailAlreadyExistsException
    ├── UsernameAlreadyTakenException
    ├── MembershipNotFoundException
    ├── UnsupportedOAuthProviderException
    └── UnauthorizedRoleAssignmentException
```

All auth exceptions extend `DomainException` through `AuthException`, providing specific error types for different failure scenarios.

## Known Limitations

1. **No session management** - OAuth2 token management not implemented
2. **Role hierarchy not supported** - Flat role structure

## Dependencies

- **Used by**: All other features (authentication required)
- **Uses**: None (foundational feature)

---

**Implementation Status**: Complete  
**OOP Maturity**: Full OOP (Reference Implementation)  
**Last Updated**: 2026-04-06
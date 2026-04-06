# Auth Module Analytics & Improvement Plan

## 1. Module Overview

The auth module provides authentication and authorization services with the following structure:

```
authModule/
├── domain/
│   ├── User.java           - User profile entity
│   ├── UserAuth.java       - Authentication credentials
│   ├── Role.java           - Role entity
│   ├── Membership.java    - Shop membership with roles
│   └── PasswordEncoder.java - Password encoding interface
├── Repository/
│   ├── UserRepository.java
│   ├── UserAuthRepository.java
│   └── MembershipRepository.java
└── service/
    ├── IAuthService.java   - Service interface
    └── AuthService.java    - Service implementation
```

## 2. Current Issues Identified

### 2.1 Exception Handling
- **Global exception usage**: `DomainException` is used for all error cases
- No specific exceptions for auth-related errors (email exists, invalid credentials, etc.)

### 2.2 Constructor Validation
- **UserAuth.java**: Partially compliant - uses setters for validation in constructor
- **User.java**: Fully compliant - uses setters in constructor
- **Role.java**: Fully compliant - uses setters in constructor
- **Membership.java**: Fully compliant - uses setters with `touch()` for audit

### 2.3 Potential Functional Interface Opportunities
- `PasswordEncoder.matches()` - could use `Predicate<String>`
- Role resolution in `AuthService` - could use `Function<UUID, Role.RoleName>`
- Membership filtering could benefit from `Predicate<Membership>`

## 3. Improvement Plan

### 3.1 Create Specific Exceptions (Priority: HIGH)

Create specific exceptions extending a base auth exception:
- `EmailAlreadyExistsException`
- `UsernameAlreadyTakenException`
- `InvalidCredentialsException`
- `UnsupportedOAuthProviderException`
- `UserNotFoundException`
- `MembershipNotFoundException`
- `UnauthorizedRoleAssignmentException`

### 3.2 Constructor Validation Compliance (Priority: HIGH)

All entities already use setters in constructors. This is already implemented correctly.

### 3.3 Functional Interface Recommendations (Priority: MEDIUM)

**Recommended**:
1. `Function<UUID, Role.RoleName>` for role resolution
2. `Predicate<Membership>` for membership filtering by shop/status
3. `BiPredicate<String, String>` for permission checks (userId, action)

**Not Recommended**:
- Replacing simple method calls with lambdas where not needed
- Over-abstracting repository interfaces

## 4. Implementation Roadmap

1. Create `AuthException.java` base class in `authModule/exception/`
2. Create specific exception classes
3. Update `AuthService` to throw specific exceptions
4. Optionally introduce functional interfaces for role resolution

## 5. Implementation Status

| Area | Status | Action Required |
|------|--------|-----------------|
| Constructor validation | ✅ Good | None |
| Exception handling | ✅ Fixed | Specific exceptions implemented |
| Functional interfaces | ⚠️ Optional | See recommendations below |

## 6. Functional Interface Recommendations

### Recommended for Use:

1. **`Function<UUID, Role.RoleName>` for role resolution**
   ```java
   private final Function<UUID, Role.RoleName> roleResolver;
   
   // Usage in constructor:
   public AuthService(..., Function<UUID, Role.RoleName> roleResolver) {
       this.roleResolver = roleResolver;
   }
   ```

2. **`Predicate<Membership>` for membership filtering**
   ```java
   // Example: filter active memberships for a shop
   Predicate<Membership> activeShopMember = m -> 
       m.getShopId().equals(shopId) && m.getRoleId() != null;
   ```

3. **`BiPredicate<UUID, Role.RoleName>` for permission checks**
   ```java
   BiPredicate<UUID, Role.RoleName> canManage = (userId, role) -> 
       role == Role.RoleName.OWNER || role == Role.RoleName.SUPER_ADMIN;
   ```

### Not Recommended:

- Replacing simple method calls with lambdas where not needed
- Over-abstracting repository interfaces
- Using lambdas for simple boolean checks that are clearer as methods

### Current Implementation:

The current `resolveActorRole` method uses method references which is idiomatic:
```java
private Role.RoleName resolveActorRole(UUID actorId) {
    return membershipRepository.findByUserId(actorId).stream()
            .map(m -> resolveRoleNameFromId(m.getRoleId()))
            .reduce(Role.RoleName.SELLER, AuthService::highestPrivilege);
}
```

This is already optimal. Introducing a `Function<UUID, Role.RoleName>` would add complexity without benefit in this case.

## 7. Files Created

- `exception/AuthException.java` - Base exception class
- `exception/EmailAlreadyExistsException.java`
- `exception/UsernameAlreadyTakenException.java`
- `exception/InvalidCredentialsException.java`
- `exception/UnsupportedOAuthProviderException.java`
- `exception/UserNotFoundException.java`
- `exception/MembershipNotFoundException.java`
- `exception/UnauthorizedRoleAssignmentException.java`

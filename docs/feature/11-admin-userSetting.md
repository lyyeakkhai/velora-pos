# Feature 11: Admin & User Settings

## System Overview

This cross-cutting administrative domain manages platform governance, user account lifecycle, shop account lifecycle, session management, and settings.

## Domain Model

### Entities/Components

#### UserAccount
User lifecycle management (referenced from Feature 1).

| Management | Operations |
|------------|------------|
| Status | ACTIVE, SUSPENDED, DELETED |
| Lifecycle | create(), suspend(), activate(), delete() |

#### ShopAccount
Shop lifecycle management (referenced from Feature 2).

| Management | Operations |
|------------|------------|
| Status | ACTIVE, BANNED, SUSPENDED |
| Lifecycle | register(), suspend(), ban(), activate() |

#### PlatformRegistry
"God Mode" admin controls.

| Control | Description |
|---------|-------------|
| Master Switch | Controls platform access |
| Ban Reason | Audit trail for bans |
| Transaction Link | Payment reference |

#### Session
Session management.

| Field | Validation |
|-------|------------|
| sessionId | Unique, immutable |
| userId | FK |
| createdAt | Timestamp |
| expiresAt | Timestamp |
| isRevoked | boolean |

#### ShopSettings
Shop configuration.

| Field | Description |
|-------|-------------|
| currency | Default USD |
| timezone | Default Asia/Phnom_Penh |
| shopName | Display name |
| logoUrl | Shop logo |

#### UserShopConfig
User-shop relationship configuration.

| Field | Description |
|-------|-------------|
| userId | FK |
| shopId | FK |
| configKey | Setting key |
| configValue | JSON value |

#### DataSyncLog
Data synchronization monitoring.

| Field | Description |
|-------|-------------|
| logId | PK |
| entityType | Synced entity |
| timestamp | Sync time |
| status | PENDING/SUCCESS/FAILED |
| errorMessage | Error details |

## Business Rules

1. Admin operations require SUPER_ADMIN role
2. Session revocation requires valid session ID
3. All user data scoped by userId
4. All shop data scoped by shopId
5. Banned users cannot access platform
6. All admin actions logged

## Implementation Details

### Operational Workflows

**User Subscription Lifecycle:**
1. User registers → create UserAccount
2. User subscribes → link to Plan
3. Subscription expires → suspend access
4. Payment fails → mark PAST_DUE

**Shop Subscription Lifecycle:**
1. Owner creates shop → create ShopAccount
2. Verify legal identity → ACTIVE status
3. Payment fails → EXPIRED status
4. Violation → BANNED

**Admin Banning/Suspension:**
1. Review violation
2. Set ban reason
3. Revoke sessions
4. Notify user
5. Log action

**Session Revocation:**
1. Admin requests revocation
2. Validate session exists
3. Mark as revoked
4. Invalidate tokens

**Settings Updates:**
1. Validate role (OWNER/ADMIN)
2. Update settings
3. Log change

**Permission Changes:**
1. Validate admin role
2. Update role in Membership
3. Log change

### Business Services

| Service | Methods |
|---------|---------|
| UserLifecycleService | createUser(), suspendUser(), activateUser(), deleteUser() |
| ShopLifecycleService | createShop(), suspendShop(), banShop(), activateShop() |
| SessionManagementService | createSession(), revokeSession(), validateSession() |
| SettingsService | getSettings(), updateSettings() |
| PermissionService | grantPermission(), revokePermission() |
| DataSyncService | syncData(), getSyncStatus() |

## Security Design

### God Mode Controls

- Only SUPER_ADMIN can access platform registry
- All admin actions require authentication
- Audit trail for all admin operations
- Role-based permission escalation prevention

### Audit Trail

- All user lifecycle changes logged
- All shop lifecycle changes logged
- All session revocations logged
- All permission changes logged

### Data Isolation

- User data isolated by userId
- Shop data isolated by shopId
- No cross-tenant access

## OOP Best Practices Applied

1. **Role-Based Access Control** - SUPER_ADMIN, OWNER, MANAGER, SELLER hierarchy
2. **Audit Trail** - All admin actions logged
3. **Session Management** - Immutable session IDs with expiration
4. **Exception Handling** - Uses DomainException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `admin/service/AdminService.java` | Admin operations service |
| `common/AbstractDomainService.java` | Base service with role validation |
| `common/DomainException.java` | Base business exception |

### Exception Handling

| Exception Type | Usage |
|---------------|-------|
| `DomainException` | Date range validation, role checks |

```
RuntimeException
└── DomainException
    └── "startInclusive must not be after endInclusive"
```

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: Medium - Service implementation with validation  
**Last Updated**: 2026-04-07
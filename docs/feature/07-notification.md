# Feature 7: Notification

## System Overview

This system delivers transactional, system, and support notifications through in-app and email channels while respecting user preferences and security requirements.

## Domain Model

### Entities

#### Notification
Core notification storage.

| Field | Type | Validation |
|-------|------|------------|
| notificationId | UUID | Immutable |
| userId | UUID | FK |
| type | NotificationType | TRANSACTIONAL/SYSTEM/SUPPORT |
| priority | Priority | HIGH/NORMAL |
| title | String | Max 255, not blank |
| content | String | Not blank |
| linkUrl | String | Nullable, valid URL |
| isRead | boolean | Default false |
| createdAt | LocalDateTime | Immutable |

#### NotificationPreferences
User notification settings.

| Field | Type | Validation |
|-------|------|------------|
| userId | UUID | PK/FK |
| emailEnabled | boolean | Default true |
| billingAlerts | boolean | Default true, immutable |
| marketingAlerts | boolean | Default false |
| updatedAt | LocalDateTime | Managed |

#### NotificationDispatchRecord
Dispatch audit log.

| Field | Type | Validation |
|-------|------|------------|
| recordId | UUID | PK |
| notificationId | UUID | FK |
| channel | Channel | IN_APP/EMAIL |
| status | DispatchStatus | PENDING/SENT/FAILED |
| dispatchedAt | LocalDateTime | Nullable |
| retryCount | int | Default 0 |

### Value Objects

| Value Object | Description |
|--------------|------------|
| NotificationType | TRANSACTIONAL, SYSTEM, SUPPORT |
| Priority | HIGH, NORMAL |
| Channel | IN_APP, EMAIL |
| DispatchStatus | PENDING, SENT, FAILED |

## Entity Relationships

```
User (1:N) Notification
User (1:1) NotificationPreferences
Notification (1:N) NotificationDispatchRecord
```

## Business Rules

1. Notifications are append-only (no delete)
2. billingAlerts is always TRUE (immutable)
3. Email dispatch only for HIGH priority AND emailEnabled = true
4. In-app dispatch always available
5. All queries scoped by userId

## State Transitions

```
Notification: created → dispatched → read
DispatchRecord: PENDING → SENT/FAILED
```

## Implementation Details

### Notification Lifecycle

1. **Trigger Phase** - Business event occurs
2. **Creation Phase** - Insert notification, isRead=false, createdAt=now()
3. **Preference Check** - Fetch preferences
4. **Dispatch Phase** - In-app always, Email only if HIGH + emailEnabled
5. **Delivery Confirmation** - Log success/failure

### Business Services

| Service | Methods |
|---------|---------|
| NotificationService | createNotification(), markAsRead(), markAllAsRead(), getUnreadCount(), getUserNotifications() |
| PreferenceService | getPreferences(), updatePreferences(), resetToDefault() |
| DispatchService | dispatchNotification(), retryFailed(), scheduleDelivery() |

### Role Access Control

| Role | Access |
|------|-------|
| USER | Read own notifications, update preferences |
| ADMIN | System notifications only |
| SYSTEM | Create notifications only |

## OOP Best Practices Applied

1. **Immutability** - notificationId and createdAt cannot be modified
2. **Append-Only** - No delete operations
3. **Reliability** - All events persisted before delivery
4. **Security** - All queries scoped by userId
5. **Preference Validation** - billingAlerts immutable, email dispatch rules

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `core/domain/notification/NotificationTesting.java` | Test stub for notification entity |

### Exception Handling

No exceptions thrown - this module is currently in design/-stub phase.

---

**Implementation Status**: Design Only (Test Stub)  
**OOP Maturity**: Low - Needs full entity implementation  

**Last Updated**: 2026-04-07
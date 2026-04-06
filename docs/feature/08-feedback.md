# Feature 8: Feedback & Suggestions

## System Overview

This feature allows users (Owners/Staff) to submit private improvement suggestions to platform administrators. It manages the suggestion lifecycle from submission to shipped status.

## Domain Model

### Entities

#### FeatureSuggestion
User-submitted improvement suggestion.

| Field | Type | Validation |
|-------|------|------------|
| suggestionId | UUID | Immutable |
| userId | UUID | FK |
| category | Category | ENUM values |
| problemText | TEXT | Not blank |
| solutionText | TEXT | Nullable |
| status | SuggestionStatus | NEW/IN_REVIEW/BACKLOG/SHIPPED |
| adminNotes | TEXT | Admin only |
| createdAt | LocalDateTime | Immutable |
| updatedAt | LocalDateTime | Managed |

### Value Objects

| Value Object | Description |
|--------------|------------|
| Category | Inventory, Finance, Staff, UI, Payment, General |
| SuggestionStatus | NEW, IN_REVIEW, BACKLOG, SHIPPED |

## Entity Relationships

```
User (1:N) FeatureSuggestion
```

## Business Rules

1. Users can only edit their own suggestions while status is NEW
2. Status transitions are forward-only (not reversible)
3. Admin notes only writable by ADMIN role
4. All queries scoped by userId

## State Transitions

```
Suggestion: NEW → IN_REVIEW → BACKLOG → SHIPPED
```

## Implementation Details

### Operational Workflows

1. **Submit Feedback** - User creates suggestion with NEW status
2. **Edit Suggestion** - Only while status is NEW
3. **Review Suggestion** - Admin changes to IN_REVIEW
4. **Add to Backlog** - Admin moves to BACKLOG
5. **Ship** - Admin marks as SHIPPED
6. **Admin Notes** - Text field for internal communication

### Business Services

| Service | Methods |
|---------|---------|
| FeedbackService | submitSuggestion(), editSuggestion(), adminUpdateStatus(), listMySuggestions(), adminListByStatus() |

### Access Control

| Role | Access |
|------|-------|
| USER | See own suggestions only |
| ADMIN | See all suggestions, update status |

## OOP Best Practices Applied

1. **Immutability** - suggestionId and createdAt immutable
2. **State Machine** - Forward-only transitions
3. **Access Control** - Role-based visibility
4. **Exception Handling** - Uses DomainException, IllegalStateException, IllegalArgumentException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `feedback/domain/FeatureSuggestion.java` | Suggestion entity with state machine |
| `feedback/domain/FeedbackService.java` | Suggestion lifecycle service |
| `feedback/domain/FeedbackAccessPolicy.java` | Role-based access control |
| `common/DomainException.java` | Base business exception |

### Exception Handling

The module uses three types of exceptions:

| Exception Type | Usage |
|---------------|-------|
| `DomainException` | Access control violations (role, ownership) |
| `IllegalStateException` | State transition violations (edit, revert) |
| `IllegalArgumentException` | Parameter validation (blank fields) |

```
RuntimeException
└── DomainException
    ├── "SUPER_ADMIN role required for: X"
    ├── "actorRole must not be null"
    ├── "Unknown operation: X"
    └── "Only the owner can modify this suggestion"

IllegalStateException (state transitions)
    ├── "Suggestion can only be edited while NEW"
    ├── "SHIPPED suggestions cannot revert"
    ├── "Invalid transition from NEW"
    ├── "Invalid transition from IN_REVIEW"
    └── "Invalid transition from BACKLOG"

IllegalArgumentException (validation)
    ├── "solutionText cannot be blank"
    └── "adminNotes cannot be blank"
```

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: High - Full entity with state machine and RBAC  
**Last Updated**: 2026-04-07
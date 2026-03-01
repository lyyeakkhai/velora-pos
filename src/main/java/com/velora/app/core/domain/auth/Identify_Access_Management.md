You are a senior Java backend architect.

Generate a complete technical documentation and implementation guide inside:

Implementation.readme

For the following folder structure:

auth/
 ├── User.java
 ├── UserAuth.java
 ├── Membership.java
 └── Role.java


====================================
📌 SYSTEM CONTEXT
====================================

This module is for Authentication, Authorization, and User Identity Management.

It is part of a scalable enterprise system.

The design must follow clean architecture and domain-driven principles.


====================================
📌 DATABASE STRUCTURE
====================================

TABLE: USERS
- username (VARCHAR, NOT NULL, UNIQUE)
- profileUrl (TEXT, NULLABLE)
- bio (TEXT, NULLABLE)
- status (ENUM: ACTIVE, SUSPENDED, DELETED)


TABLE: USER_AUTH
- authID (UUID/BIGINT, PK)
- provider (ENUM: EMAIL, GOOGLE, FACEBOOK)
- provider_uid (VARCHAR, NULLABLE)
- email (VARCHAR, UNIQUE)
- password_hash (TEXT, NULLABLE)
- created_at (TIMESTAMP, NOT NULL)
- last_login_at (TIMESTAMP, NULLABLE)
- user_id (FK → USERS)


TABLE: MEMBERSHIPS
- memberID (UUID/BIGINT, PK)
- sellerName (VARCHAR, NULLABLE)
- createAt (TIMESTAMP, NOT NULL)
- updateAt (TIMESTAMP, NOT NULL)
- user_id (FK → USERS)
- shop_id (FK → SHOPS)
- role_id (FK → ROLES)


TABLE: ROLES
- role_id (UUID/BIGINT, PK)
- role_name (ENUM: OWNER, MANAGER, SELLER)


====================================
📌 IMPLEMENTATION RULES (MANDATORY)
====================================

1. ❌ NO default constructor allowed
2. ✅ Every class MUST use parameterized constructors
3. ✅ All validation logic MUST be inside constructors and setters
4. ✅ Setter methods MUST validate:
   - null
   - empty
   - invalid format
   - illegal state
5. ✅ Immutable fields where possible
6. ✅ Clean OOP principles
7. ✅ SOLID compliant
8. ✅ Scalable and maintainable
9. ✅ No hardcoding
10. ✅ No business logic in entity classes


====================================
📌 CLASS REQUIREMENTS
====================================

Each class must contain:

- Private fields
- Private validation methods
- Public getters
- Controlled setters with validation
- Constructor validation
- toString()
- equals() and hashCode()
- Clear comments


------------------------------------
User.java
------------------------------------
Represents the main user profile.

Fields:
- id
- username
- profileUrl
- bio
- status

Responsibilities:
- Profile management
- User lifecycle state


------------------------------------
UserAuth.java
------------------------------------
Represents authentication credentials.

Fields:
- authId
- provider
- providerUid
- email
- passwordHash
- createdAt
- lastLoginAt
- userId

Responsibilities:
- Login identity
- Provider management
- Security metadata


------------------------------------
Membership.java
------------------------------------
Represents user's membership in a shop.

Fields:
- memberId
- sellerName
- createdAt
- updatedAt
- userId
- shopId
- roleId

Responsibilities:
- Staff identity
- Shop access control


------------------------------------
Role.java
------------------------------------
Represents system role.

Fields:
- roleId
- roleName

Responsibilities:
- Permission classification


====================================
📌 VALIDATION RULES
====================================

Username:
- 3–30 characters
- Alphanumeric + underscore
- No spaces

Email:
- RFC 5322 compliant regex

Password Hash:
- Must not be raw password
- Must be >= 60 chars (bcrypt)

UUID/BIGINT:
- Must be positive
- Must not be null

URL:
- Must be valid HTTP/HTTPS

Timestamps:
- Cannot be future date

Status:
- Enum only


====================================
📌 DOCUMENTATION REQUIREMENT
====================================

Inside Implementation.readme, provide:

1️⃣ System Overview
2️⃣ Architecture Diagram (ASCII)
3️⃣ Entity Relationship Explanation
4️⃣ Class Responsibility Table
5️⃣ Validation Strategy
6️⃣ Security Design
7️⃣ Scalability Notes
8️⃣ Extension Guide
9️⃣ Common Mistakes
10️⃣ Future Improvements


====================================
📌 OUTPUT FORMAT
====================================

1. First generate:
   - Implementation.readme (Markdown format)

2. Then generate:
   - User.java
   - UserAuth.java
   - Membership.java
   - Role.java

3. Each Java file must include:
   - Package name: auth
   - JavaDoc class comments
   - Method comments
   - Clean formatting


====================================
📌 CODE QUALITY
====================================

- Follow Java 17 standards
- Use Optional where needed
- Use LocalDateTime for timestamps
- Use UUID where applicable
- No Lombok
- No framework annotations
- Pure Java OOP


====================================
📌 FINAL GOAL
====================================

Produce enterprise-grade authentication domain entities that:

✔ Are secure  
✔ Are extensible  
✔ Are production-ready  
✔ Are easy to test  
✔ Follow best practices  

Do NOT simplify.

Do NOT skip validations.

Do NOT remove documentation.

Generate full implementation.
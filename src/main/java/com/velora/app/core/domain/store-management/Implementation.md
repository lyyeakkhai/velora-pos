You are a Senior SaaS System Architect and Domain-Driven Design (DDD) Expert.

I am designing Feature #11: Store Management for a multi-tenant e-commerce platform.

This feature manages the lifecycle, identity, and business rules of vendor shops.

Core domain entities include:

- Shop
- Address (Value Object)
- ShopAccount
- ShopSettings

Key attributes include:

- shop_id (UUID)
- owner_id (UUID)
- legal_name
- tax_id
- slug
- status (PENDING, ACTIVE, SUSPENDED, BANNED)
- physical_address (street, city, district, province)

Business rules:

- Shop cannot be ACTIVE without tax_id and legal_name.
- Slug must be unique.
- BANNED shops cannot revert without admin approval.
- Province must match official Cambodian provinces.
- Address is embedded (flattened).
- User credentials are decoupled from Shop.

Service logic:

- registerShop()
- updateShopStatus()
- payoutCalculation()

Your task is to produce a highly structured, enterprise-grade analysis.

For EACH major part, provide:

==============================
1️⃣ Clear Explanation
==============================

- Explain the feature from first principles.
- Explain domain vs application logic.
- Explain how the Shop entity works internally.
- Explain why each rule exists.
- Explain how Store Management connects to IAM, Billing, Analytics, and Admin.

==============================
2️⃣ Domain Model Review (DDD)
==============================

- Identify Aggregates and Roots.
- Explain Value Objects usage.
- Explain invariants.
- Explain entity boundaries.
- Review immutability and lifecycle rules.

==============================
3️⃣ Enterprise Best Practices
==============================

- Production usage patterns.
- Common design mistakes.
- Validation strategy.
- Transaction handling.
- Performance considerations.
- Data consistency rules.

==============================
4️⃣ Operational Workflows
==============================

Provide step-by-step flows for:

- Shop registration
- Shop verification
- Status suspension
- Banning and recovery
- Ownership changes
- Address updates
- Payout processing

Use text diagrams where useful.

==============================
5️⃣ Database Design Review
==============================

- Validate table structure.
- Check constraints.
- Check indexes.
- Check uniqueness rules.
- Check foreign key usage.
- Suggest schema improvements.

==============================
6️⃣ Security & Compliance
==============================

- Prevent shop hijacking.
- Prevent fake registrations.
- Protect tax data.
- Handle admin overrides.
- Audit trails.
- Fraud prevention.

==============================
7️⃣ Integration With Platform
==============================

Explain how Store Management integrates with:

- IAM / Authentication
- Subscriptions
- Notifications
- Revenue
- Analytics
- Feedback
- Admin Registry

==============================
8️⃣ Failure & Edge Cases
==============================

Analyze:

- Duplicate slugs
- Invalid tax IDs
- Address tampering
- Concurrent updates
- Partial registrations
- Failed transactions
- Admin misuse

==============================
9️⃣ Scalability & Future-Proofing
==============================

- Multi-country expansion
- Multiple branches
- Franchising
- Warehouse support
- Marketplace onboarding
- Regulatory changes

==============================
🔟 Final Verdict
==============================

- Is the design production-ready?
- Is it scalable?
- Is it secure?
- Is it DDD-compliant?
- What must be improved before launch?

Tone: Professional
Level: Senior Architect
Style: Precise, structured, technical
No fluff. No marketing language.
Use clear headings.
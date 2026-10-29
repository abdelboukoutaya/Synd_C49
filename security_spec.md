# Security Specification — Syndicat C49

## 1. Data Invariants
1. **Strict User Isolation**: Every document in `/users/{userId}/settings/{settingId}`, `/users/{userId}/apartments/{apartmentId}`, and `/users/{userId}/expenses/{expenseId}` belongs exclusively to `userId` (`request.auth.uid == userId` and `data.userId == request.auth.uid`).
2. **Schema & Key Allowlist**: Every `create` and `update` must satisfy `isValidSyndicatConfig`, `isValidApartmentRecord`, or `isValidExpenseRecord`, enforcing `hasAll` and `hasOnly` key allowlists, string `.size()` bounds, integer range bounds, enum checks, and server timestamp upper bounds (`<= request.time`).
3. **Immutable Identity & Creation Metadata**: On `update`, `userId`, `createdAt` (and `number`/`floor` on apartments) are strictly immutable (`incoming().field == existing().field`).
4. **Query Enforcer on `list`**: `allow list` verifies `isOwner(userId) && existing().userId == request.auth.uid` so queries must align with `.whereEqualTo("userId", uid)` and never rely on blanket reads.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `GET /users/alice_123/apartments/appt_01` with `auth = null` -> REJECTED.
2. **Cross-User Read**: `GET /users/bob_456/apartments/appt_01` as `alice_123` -> REJECTED.
3. **Identity Spoofing on Create**: `CREATE /users/alice_123/expenses/exp_1` with `{"userId": "bob_456", ...}` as `alice_123` -> REJECTED.
4. **Shadow/Ghost Field Injection**: `UPDATE /users/alice_123/apartments/appt_01` adding `{"isAdmin": true}` -> REJECTED by `hasOnly`.
5. **Immutable `createdAt` Tampering**: `UPDATE /users/alice_123/expenses/exp_1` modifying `createdAt` -> REJECTED.
6. **Immutable Apartment Number Mutation**: `UPDATE /users/alice_123/apartments/appt_01` changing `number` from `1` to `99` -> REJECTED.
7. **Negative Payment Injection**: `CREATE /users/alice_123/apartments/appt_01` with `{"juin": -500}` -> REJECTED.
8. **Invalid Floor Enum**: `CREATE /users/alice_123/apartments/appt_01` with `{"floor": "10e"}` -> REJECTED.
9. **Invalid Expense Category Enum**: `CREATE /users/alice_123/expenses/exp_1` with `{"category": "Crypto"}` -> REJECTED.
10. **Oversized String DoW Attack**: `CREATE /users/alice_123/expenses/exp_1` with `title` of 5,000 chars -> REJECTED.
11. **ID Poisoning Attack**: `CREATE /users/alice_123/expenses/bad$id!` -> REJECTED by `isValidId`.
12. **Unfiltered List Scraping**: `LIST /users/alice_123/apartments` without `.where("userId", "==", "alice_123")` -> REJECTED.

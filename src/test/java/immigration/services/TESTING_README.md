# Testing README — White-box Reasoning

## Overview

Tests are distributed across two packages:
- `immigration.validators` — unit tests for all four validator classes
- `immigration.services`   — integration-style unit tests for `VerificationService` and `ShareCodeService`

All service tests use `@TempDir` (JUnit 5) to create isolated JSON files per test run, ensuring no shared state between tests.

---

## Validator Tests

### ShareCodeValidatorTest

Covers three independent static methods. Each has its own equivalence classes:

| Method | EC (valid) | EC (invalid) | BVA |
|--------|-----------|-------------|-----|
| `validateFormat` | 9-char uppercase alphanum | null, empty, lowercase, symbols | 8 chars (low), 10 chars (high) |
| `validateNotExpired` | future expiry | past expiry | — |
| `validateNotUsed` | used=false | used=true | — |

**State-transition coverage**: `validateNotUsed` tests the two states of the `used` flag (false → pass, true → fail), matching the state-transition diagram: `UNUSED → USED` after one successful verification.

**Branch coverage**: `validateFormat` has one conditional; tests exercise both the regex-match branch (pass) and two distinct non-match branches (wrong length, wrong character class).

---

### DocumentValidatorTest

Two independent validators with symmetric structure:

| Method | Boundary tests | Equivalence classes |
|--------|---------------|-------------------|
| `validatePassport` | 8 chars (fail), 9 chars (pass), 10 chars (fail) | lowercase, space, null |
| `validatePermit` | 8 total chars (fail), 9 chars (pass) | all digits, 3 letters, lowercase, null |

All branches in each `if` statement are covered by at least one test.

---

### OrganisationValidatorTest

Decision-table technique: all role × purpose combinations are systematically covered.

| Role | Share Code Route | Document Route | EMPLOYMENT | ACCOMMODATION | EDUCATION |
|------|:---:|:---:|:---:|:---:|:---:|
| EMPLOYER | ✓ | ✗ | ✓ | ✗ | — |
| LANDLORD | ✓ | ✗ | ✗ | ✓ | — |
| EDUCATION | ✓ | ✗ | — | — | ✓ |
| BORDER_CONTROL | ✗ | ✓ | — | — | — |
| LAW_ENFORCEMENT | ✗ | ✓ | — | — | — |

Each cell with ✓ or ✗ has a corresponding test. Unknown purpose is also tested.

---

### DobValidatorTest

Targets the sequential decision path in `DobValidator.validate`:
1. null/blank check
2. input parse (format validation)
3. equality comparison

BVA applied at the day boundary (1985-03-22 vs 1985-03-23 → fail).
White-box note: trimming of leading/trailing spaces is verified by a dedicated test because the branch `inputDob.trim()` is internal to the method.

---

## Service Tests

### VerificationServiceTest

Covers all 13 rejection branches and 4 success outcomes of `verifyByShareCode` and `verifyByDocument`.

**Path coverage of `verifyByShareCode`** (in order of guard evaluation):
1. Unknown org → `Rejected` ✓
2. Wrong role (BORDER_CONTROL) → `Rejected` ✓
3. Bad format code → `Rejected` ✓
4. Expired code → `Rejected` ✓
5. Used code → `Rejected` ✓
6. Wrong DOB → `Rejected` ✓
7. Purpose mismatch → `Rejected` ✓
8. Success (EMPLOYER) → `RightToWork` ✓
9. Success (LANDLORD) → `RightToRent` ✓
10. Success (visitor, no rights) → `RightToWork{eligible=false}` ✓

**State-transition**: `validShareCode_marksCodeUsed` verifies that the code transitions from UNUSED to USED after one successful verification and that a subsequent attempt is rejected — exercising the `validateNotUsed` branch inside the service flow.

**Document path coverage**:
1. Unknown org → `Rejected`
2. Wrong role (EMPLOYER) → `Rejected` ✓
3. Invalid format → `Rejected` ✓
4. Doc not found → `Rejected` ✓
5. BORDER_CONTROL success → `EntryPermission` ✓
6. LAW_ENFORCEMENT success → `StatusValidity` ✓

---

### ShareCodeServiceTest

Focuses on the generation contract:
- Format invariant (`^[A-Z0-9]{9}$`)
- Persistence (code retrievable after generation)
- Expiry window (~30 days, verified with time bounds)
- Uniqueness (two sequential calls produce different codes — probabilistic, not deterministic, but failure probability is 1/32^9 ≈ 10^-13)
- State (used=false on creation)
- Purpose fidelity

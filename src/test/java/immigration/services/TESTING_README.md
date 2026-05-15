# Testing README — White-box Reasoning

## Overview

Tests are distributed across four packages:
- `immigration.validators` — unit tests for all four validator classes
- `immigration.services`   — integration-style unit tests for `VerificationService` and `ShareCodeService`
- `immigration.api`        — end-to-end HTTP tests against a live `ApiServer` spun up on an ephemeral port
- `immigration.cli`        — end-to-end tests that drive CLI menus via simulated input against the same live server

All service tests use `@TempDir` (JUnit 5) to create isolated JSON files per test run, ensuring no shared state between tests.

---

## Validator Tests

### ShareCodeValidatorTest

Covers two independent static methods. Each has its own equivalence classes:

| Method | EC (valid) | EC (invalid) | BVA |
|--------|-----------|-------------|-----|
| `validateFormat` | 9-char uppercase alphanum | null, empty, lowercase, symbols | 8 chars (low), 10 chars (high) |
| `validateNotExpired` | future expiry | past expiry | exactly at boundary (100 ms remaining → pass) |

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

Covers rejection branches and success outcomes of `verifyByShareCode` and `verifyByDocument`.

**Path coverage of `verifyByShareCode`** (in order of guard evaluation):
1. Unknown org → `Rejected` ✓
2. Wrong role (BORDER_CONTROL) → `Rejected` ✓
3. Bad format code → `Rejected` ✓
4. Expired code → `Rejected` ✓
5. Wrong DOB → `Rejected` ✓
6. Purpose mismatch → `Rejected` ✓
7. Success (EMPLOYER) → `RightToWork` ✓
8. Success (LANDLORD) → `RightToRent` ✓
9. Success (EDUCATION) → `RightToWork` ✓
10. Success (visitor, no rights) → `RightToWork{eligible=false}` ✓
11. Person with no visa record → `Rejected` ✓

**Reusability**: `validShareCode_isReusableWithinWindow` verifies that the same code can be presented multiple times within its validity window and is approved each time — codes are not marked used after verification.

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
- Purpose fidelity

---

## End-to-End Tests

### API tests (`immigration.api`)

`ApiTestBase` starts a real `ApiServer` on an ephemeral port (port 0) with temp JSON files written to a `@TempDir`. Each test class sends real HTTP requests via `java.net.http.HttpClient` and asserts on the JSON responses. `FullWorkflowApiTest` covers multi-step scenarios (e.g. generate a share code then verify it) entirely over HTTP.

### CLI tests (`immigration.cli`)

`BaseCliTest` also starts a real `ApiServer` and connects an `HttpApiClient` to it. Tests construct a `CliDriver` with the menu inputs pre-loaded, instantiate the relevant menu class (`AdminMenu`, `ShareCodeMenu`, etc.) pointing at that client, and capture stdout with `CapturedOutput`. `FullWorkflowTest` covers the deepest paths — e.g. generating a share code through `ShareCodeMenu` and verifying it through `AdminMenu` in the same test — and also inspects the audit repository directly to assert event fields.

Key design points:
- Both suites use `@TempDir` and `@TestInstance(PER_CLASS)` so the server starts once per class, not per test.
- Share codes are reusable: `generateThenUseTwice_bothSucceed` confirms that verifying the same code twice within its validity window succeeds both times.

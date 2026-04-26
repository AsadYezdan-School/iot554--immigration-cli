# Immigration Status Verification CLI

A Java 25 console prototype demonstrating controlled immigration-status verification for a national immigration authority. 
Supports a share-code route (individual-initiated) and a document route (authority-initiated), with file-based persistence and a JSONL audit trail.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 25  |
| Maven | 3.8+ |

---

## Running the Application

```bash
mvn compile
mvn exec:java
```

The menu system guides you through all available operations.

### Seed data included

| Organisation | ID | Role |
|--------------|----|------|
| Acme Ltd | ORG001 | EMPLOYER |
| Beta Corp | ORG002 | EMPLOYER |
| City Rentals | ORG003 | LANDLORD |
| Metro College | ORG004 | EDUCATION |
| Port Authority | ORG005 | BORDER\_CONTROL |
| Metro Police | ORG006 | LAW\_ENFORCEMENT |

| Share Code | Person | DOB | Purpose | Status |
|-----------|--------|-----|---------|--------|
| `ABC123XY1` | P001 Emma Harrison | 1985-03-22 | EMPLOYMENT | Valid (unused) |
| `DEF456YZ2` | P004 Luca Ferretti | 1968-05-01 | ACCOMMODATION | Valid (unused) |
| `GHJ789ZA3` | P002 Ravi Patel | 1998-07-14 | EDUCATION | Valid (unused) |
| `KLM012BC4` | P001 | — | EMPLOYMENT | **Expired** |
| `NOP345DE5` | P004 | — | ACCOMMODATION | **Already used** |
| `QRS678FG6` | P005 Mei Zhang | 2000-09-25 | EMPLOYMENT | Valid (visitor — no work rights) |

| Passport / Permit | Person | Role required |
|-------------------|--------|--------------|
| `AB1234567` | Emma Harrison | BORDER\_CONTROL / LAW\_ENFORCEMENT |
| `EF3456789` | Luca Ferretti | BORDER\_CONTROL / LAW\_ENFORCEMENT |
| `GH5678901` | Mei Zhang | BORDER\_CONTROL / LAW\_ENFORCEMENT |
| `CD1234567` (permit) | Ravi Patel | BORDER\_CONTROL / LAW\_ENFORCEMENT |

---

## Running Tests

```bash
mvn test
```

Test output is printed to the console. Each test class exercises a distinct validator or service layer.

---

## Viewing Log Output

Application log (structured, rotating):
```bash
cat logs/app.log
```

Audit trail (append-only JSONL, one event per line):
```bash
cat data/audit_log.jsonl
```

Pretty-print individual audit events:
```bash
cat data/audit_log.jsonl | while IFS= read -r line; do echo "$line" | python3 -m json.tool; done
```

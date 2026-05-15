# Immigration Status Verification

A Java 25 console application demonstrating controlled immigration-status verification. Supports a share-code route (individual-initiated) and a document route (authority-initiated), with a structured audit trail.

## Prerequisites

| Tool  | Version |
|-------|---------|
| Java  | 25      |
| Maven | 3.8+    |

## Running the application

Starts the API server and CLI together, from the application root.

```bash
mvn compile exec:java -Pdev
```

## Running tests

```bash
mvn test
```

## Logs

Application log:
```bash
cat logs/app.log
```

Audit trail (one JSON event per line):
```bash
cat data/audit_log.jsonl
```

# Security Guidelines

We build security in from the start so we do not have to bolt it on at 2:00 AM with cold coffee and regret.

## 1. Security Baseline

- We never commit secrets to source control.
- We apply least privilege for service credentials and automation tokens.
- We treat dependency risk as a shipping blocker when severity is critical.
- We keep audit trails for meaningful security decisions.

## 2. Secret Management

- We store secrets in approved secret stores and CI secret variables.
- We rotate shared credentials on a regular schedule.
- We mask sensitive values in logs and pipeline output.
- We document secret ownership and rotation responsibility.

## 3. Dependency and Supply Chain Hygiene

- We scan dependencies in CI for known vulnerabilities.
- We patch critical vulnerabilities before release unless formally waived.
- We keep third-party dependencies minimal and justified.
- We track temporary risk acceptance in pull request notes.

## 4. Code Review Security Checks

During review, we verify:

- Input validation is present on external boundaries.
- Error handling does not leak sensitive data.
- Authentication and authorization behavior is unchanged unless intended.
- Retry logic avoids accidental denial-of-service amplification.

## 5. Logging and Observability Safety

- We log enough context to investigate incidents.
- We avoid sensitive payload logging.
- We use correlation identifiers for traceability.
- We keep security-relevant events searchable.

## 6. BDD Security Scenarios

We include behavior scenarios for critical security expectations.

Example:

```gherkin
Feature: Secret-safe logging

  Scenario: Sync failure does not expose credentials in logs
    Given we configure a connector with authentication credentials
    And the connector fails during handshake
    When we capture failure logs
    Then logs include correlation identifiers
    And logs do not include tokens, passwords, or secret values
```

## 7. Incident Readiness

- We define an escalation path for critical findings.
- We keep runbooks for containment and recovery.
- We document lessons learned in LESSONS.md after incidents.

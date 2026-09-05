# Testing Guide

We test behavior, not luck. Our goal is to make production surprises boring and rare.

## 1. Testing Principles

- We use BDD with Gherkin from unit tests through end-to-end validation.
- We keep tests readable enough that a junior contributor can understand intent quickly.
- We prefer deterministic tests over clever tests.
- We treat flaky tests as defects and fix them quickly.

## 2. Test Pyramid and Coverage

- Unit tests: largest layer, fast feedback, isolated logic.
- Integration tests: connector behavior, retry logic, persistence, and service boundaries.
- End-to-end tests: full synchronization journeys and user-visible outcomes.

Coverage target:

- Minimum 80 percent coverage for changed Java/Python/Groovy logic.
- Critical reliability paths (retry, timeout, reconciliation) require explicit scenario coverage.

## 3. BDD and Gherkin Usage

We describe behavior once and validate it at multiple layers.

- We keep feature files focused on business behavior.
- We write one behavior focus per scenario.
- We avoid implementation details in Given/When/Then wording.

Example:

```gherkin
Feature: Synchronization resiliency

  Scenario: Retry transient target failures
    Given we start a sync job for tenant A
    And the target service returns a transient 429 error twice
    When we run the synchronization
    Then we retry with exponential backoff and jitter
    And the run completes without duplicate writes
```

## 4. Unit Testing Standards

- We follow Arrange/Act/Assert.
- We mock only external dependencies, not domain logic.
- We name tests by behavior outcome.
- We include negative and edge-case coverage for every public method touched.

Naming examples:

- `shouldRetryWhenTargetReturnsTransientRateLimit`
- `shouldFailFastWhenConfigurationIsMissingRequiredEndpoint`

## 5. Integration Testing Standards

- We validate connector contracts and serialization boundaries.
- We include timeout and retry behavior checks.
- We verify idempotency for retried operations.
- We prefer containerized dependencies where feasible.

## 6. End-to-End Testing Standards

- We test complete sync journeys from source read to target verification.
- We include at least one unhappy-path e2e scenario per critical flow.
- We verify auditability signals (logs, identifiers, and status transitions).

## 7. Performance and Reliability Checks

- We run targeted performance checks for changed critical paths.
- We track latency and throughput trends for sync-heavy components.
- We fail the pipeline on severe regressions agreed by the team.

## 8. Manual Testing Guide

Before merge, we run a short manual checklist for high-risk changes:

- Start service with environment config for the target profile.
- Execute at least one representative sync journey.
- Verify logs include operation IDs and actionable error context.
- Validate rollback or recovery behavior for forced failures.

## 9. CI Expectations

Our CI should enforce:

- Unit tests on every pull request.
- Integration tests for connector and persistence changes.
- End-to-end smoke suite on pull request or nightly schedule.
- Coverage report and quality gate checks.

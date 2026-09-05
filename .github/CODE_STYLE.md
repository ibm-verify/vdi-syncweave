# Code Style Guide

We use this guide to keep our changes simple, readable, and easy to modify. If a style choice does not improve clarity, we keep it out.

## 1. Core Rules

- We prefer clear names over clever names.
- We make small methods and small commits.
- We avoid duplication and extract shared logic when the second repeat appears.
- We optimize for junior readability first, then for elegance.
- We keep comments focused on intent and tradeoffs, not obvious mechanics.

## 2. File and Naming Conventions

### Java

- Class names: `UpperCamelCase`.
- Method and field names: `lowerCamelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Interfaces describe capability (for example, `RetryPolicy`).
- Implementations state strategy (for example, `ExponentialBackoffRetryPolicy`).

### Python

- Modules and functions: `snake_case`.
- Classes: `PascalCase`.
- Constants: `UPPER_SNAKE_CASE`.
- We keep scripts import-safe with `if __name__ == "__main__":`.

### Groovy

- Follow Java naming for classes and methods.
- Keep dynamic behavior explicit in names and comments.
- Prefer small closures with descriptive parameter names.

### XML and YAML

- Use consistent 2-space indentation.
- Keep keys and element names descriptive and stable.
- Group related properties and include a short section comment when context is not obvious.

### Shell

- Use lowercase snake_case variable names for local variables.
- Use uppercase names only for exported environment variables.
- Begin scripts with strict mode when possible:

```bash
set -euo pipefail
```

## 3. Structure and Formatting

- Maximum line length target: 120.
- Keep one responsibility per method.
- Prefer guard clauses over deep nesting.
- Avoid more than 3 nested control blocks.
- Keep imports sorted and remove unused imports.

## 4. Error Handling and Logging

- We fail fast on invalid input.
- We wrap low-level exceptions with domain context.
- We log actionable context (operation, identifiers, retry attempt, elapsed time).
- We never log secrets, tokens, or full credentials.

## 5. BDD and Test Style

We use BDD language from unit tests through end-to-end tests.

- Test names should describe behavior, not implementation.
- Arrange/Act/Assert is mandatory for unit tests.
- We store Gherkin scenarios in feature files for integration and e2e coverage.
- We keep one primary behavior per scenario.

Example scenario:

```gherkin
Feature: Retry on transient sync failures

  Scenario: Sync retries when target endpoint returns rate limit
    Given we start a synchronization run for tenant A
    And the target endpoint returns HTTP 429 twice
    When we execute the sync operation
    Then we retry using exponential backoff
    And we complete the synchronization without data loss
```

## 6. DRY and Simplicity Checklist

Before we open a pull request, we ask:

- Did we remove duplicate logic?
- Did we pick the simplest design that solves the real problem?
- Can a junior contributor explain the flow in under 2 minutes?
- Would we be comfortable maintaining this in six months?

If any answer is no, we refactor before merge.

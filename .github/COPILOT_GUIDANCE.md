# Copilot Guidance

We use AI assistance as a junior-friendly pair-programming accelerator, not as a substitute for engineering judgment.

## 1. Intent

- We ask for simple, maintainable, DRY solutions.
- We ask for step-by-step reasoning that junior contributors can follow.
- We ask for behavior-driven suggestions aligned with BDD scenarios.

## 2. Required Context for AI Prompts

When we ask for code help, we include:

- The business behavior we are changing.
- The expected Given/When/Then outcome.
- Any reliability constraints (retries, timeouts, idempotency).
- Any security constraints (secrets, auth, auditability).

## 3. Required AI Output Qualities

- Proposals are small, explicit, and easy to review.
- Suggested code follows code style and architecture guides.
- Suggested tests include or reference Gherkin behavior.
- Suggested changes call out risk and rollback impact.

## 4. Review Rules for AI-Generated Changes

Before merge, we verify:

- The change is understandable by a junior contributor.
- The change does not duplicate existing logic.
- The change includes appropriate test updates.
- The change honors Git Flow and branch protections.
- The change does not introduce secrets or unsafe logging.

## 5. Local Instructions File

We keep local assistant behavior in .vscode/copilot-instructions.md, which is intentionally gitignored.

That local file should mirror this guide and include machine-specific workflow details as needed.

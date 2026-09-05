# CHANGE.md

## 2026-09-05 (Planned Before Edits)

We plan to update our project guidance to reflect the team decisions below before any additional implementation topics:

- We follow the 12 Factor Application principles.
- We apply Gang of Four design patterns where they improve clarity and maintainability.
- We use BDD (Gherkin) from unit testing through end-to-end testing.
- We follow Git Flow branching and avoid direct commits to main and develop.
- We prioritize elegant changes that are simple, easy to change, and DRY.
- We capture decisions and direction in LESSONS.md.
- We document all code and files in first person plural, as a journey junior contributors can follow, with an informal tone and light humor.

## 2026-09-05 (Completed)

- Added this file and recorded planned guidance updates before editing other project guidance files.
- Updated `.github/INSTRUCTIONS.md` with explicit rules for Git Flow, no direct commits to main/develop, 12 Factor alignment, GoF pattern usage, BDD expectations, and documentation style.
- Updated `.github/README.md` to match the same operating model for contributors and maintainers.
- Added `LESSONS.md` and seeded it with the current team baseline decisions.

## 2026-09-05 (Planned Before Edits - Wave 2)

We plan to add focused implementation guidance for the next topics:

- Create `.github/CODE_STYLE.md` with language-specific standards for Java, Python, Groovy, XML/YAML, and shell scripts.
- Create `.github/ARCHITECTURE.md` with 12 Factor alignment, GoF usage guidance, resilience patterns, and BDD traceability from behavior to tests.
- Update `.github/README.md` to include quick links to the new guidance files.

## 2026-09-05 (Completed - Wave 2)

- Added `.github/CODE_STYLE.md` with coding conventions, formatting rules, logging/error handling guidance, and BDD test style.
- Added `.github/ARCHITECTURE.md` with 12 Factor alignment, preferred GoF patterns, reliability/concurrency guidance, and BDD traceability expectations.
- Updated `.github/README.md` so contributors can quickly find and apply the new guidance.

## 2026-09-05 (Planned Before Edits - Wave 3)

We plan to add the next instruction topics:

- Create `.github/TESTING.md` covering BDD with Gherkin across unit, integration, and end-to-end testing.
- Create `.github/TOOLING_RECOMMENDATIONS.md` with lightweight linting, testing, and CI tooling that matches our stack.
- Update `.github/README.md` with quick links to these new guides.

## 2026-09-05 (Completed - Wave 3)

- Added `.github/TESTING.md` with BDD-first testing guidance from unit to end-to-end and reliability-focused validation expectations.
- Added `.github/TOOLING_RECOMMENDATIONS.md` with practical tooling recommendations for Java, Groovy, Python, shell, config, and CI quality gates.
- Updated `.github/README.md` to include discovery links and contributor usage guidance for the new files.

## 2026-09-05 (Planned Before Edits - Wave 4)

We plan to add deployment and security guidance:

- Create `.github/DEPLOYMENT.md` with Git Flow-aware release, promotion, and rollback guidance.
- Create `.github/SECURITY_GUIDELINES.md` with baseline security controls, review checkpoints, and dependency hygiene rules.
- Update `.github/README.md` with links to these new guides.

## 2026-09-05 (Completed - Wave 4)

- Added `.github/DEPLOYMENT.md` with branch-to-environment flow, release checks, validation gates, and rollback guidance.
- Added `.github/SECURITY_GUIDELINES.md` with secret handling, dependency hygiene, secure review checks, and security BDD scenario guidance.
- Updated `.github/README.md` to include quick access to deployment and security guidance.

## 2026-09-05 (Planned Before Edits - Wave 5)

We plan to add AI guidance and enforcement scaffolding:

- Create `.github/COPILOT_GUIDANCE.md` with tracked team guidance for AI-assisted development.
- Create local `.vscode/copilot-instructions.md` with repository-specific assistant instructions.
- Create `.github/ENFORCEMENT.md` and a lightweight workflow scaffold to verify policy files are present.
- Update `.github/README.md` to reference the new guidance.

## 2026-09-05 (Completed - Wave 5)

- Added `.github/COPILOT_GUIDANCE.md` with team-level guidance for AI-assisted coding and review.
- Updated local `.vscode/copilot-instructions.md` to align with 12 Factor, GoF, BDD, Git Flow, and junior-friendly documentation style.
- Added `.github/ENFORCEMENT.md` plus `.github/workflows/policy-checks.yml` for lightweight policy presence checks in CI.
- Updated `.github/README.md` to include the new Copilot and enforcement guidance links.

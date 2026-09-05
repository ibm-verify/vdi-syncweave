# Tooling Recommendations

We keep our tooling lightweight, practical, and easy for new contributors to run.

## 1. Java and Groovy Tooling

- Build: Maven (primary) and Ant where required by existing modules.
- Unit test: JUnit 5.
- Mocking: Mockito.
- Static analysis: SpotBugs and PMD.
- Style checks: Checkstyle.
- Formatting: Spotless Maven plugin.

Suggested Maven plugin set:

- `maven-surefire-plugin` for unit tests.
- `maven-failsafe-plugin` for integration tests.
- `jacoco-maven-plugin` for coverage.
- `maven-checkstyle-plugin` for style policy.
- `spotbugs-maven-plugin` for defect scanning.
- `maven-enforcer-plugin` for version and dependency guardrails.

## 2. Python Tooling

- Unit test: pytest.
- Linting: pylint.
- Formatting: black.
- Import sorting: isort.
- Optional fast lint path: ruff.

## 3. Shell and Config Tooling

- Shell linting: ShellCheck.
- YAML linting: yamllint.
- XML validation: existing build validation plus schema checks where available.

## 4. BDD Tooling

- Gherkin feature files in repository-level test directories.
- Java stack option: Cucumber JVM.
- Python stack option: behave.
- We keep Gherkin language aligned with the behavior described in TESTING.md.

## 5. CI Gate Recommendations

Required on pull requests:

- Build and compile checks.
- Unit test suite with coverage report.
- Lint and static analysis checks.
- Security dependency scan.

Recommended on pull requests or nightly schedule:

- Integration tests.
- End-to-end smoke tests.
- Performance baseline checks for critical flows.

## 6. Security and Dependency Hygiene

- Enable dependency scanning in CI.
- Fail builds on critical vulnerabilities unless explicitly waived.
- Record waivers with rationale in pull request notes.

## 7. Local Developer Quick Start

We should provide one command path per language stack where possible:

- Java quick path: compile, unit tests, style checks.
- Python quick path: lint, unit tests.
- Full verification path: compile, tests, lint, and static analysis.

The exact command scripts can be added in a later wave after we map each module build path.

# Policy Enforcement Guide

We use lightweight automation to prevent drift from our standards while keeping contributor friction low.

## 1. What We Enforce Automatically

- Required policy files exist.
- Feature guidance files remain discoverable from .github/README.md.
- Pull requests target the expected base branch for normal feature work.

## 2. What We Enforce Through Review

- 12 Factor alignment quality.
- Appropriate GoF pattern usage.
- BDD scenario quality and coverage relevance.
- Simplicity and DRY decisions.

## 3. Required Files

- CHANGE.md
- LESSONS.md
- .github/INSTRUCTIONS.md
- .github/CODE_STYLE.md
- .github/ARCHITECTURE.md
- .github/TESTING.md
- .github/TOOLING_RECOMMENDATIONS.md
- .github/DEPLOYMENT.md
- .github/SECURITY_GUIDELINES.md
- .github/COPILOT_GUIDANCE.md

## 4. CI Scaffolding

We include a lightweight workflow in .github/workflows/policy-checks.yml that validates required files.

As we mature, we can extend it to verify branch and PR policies through repository settings and additional status checks.

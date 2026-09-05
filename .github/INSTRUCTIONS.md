---
applyTo: '**'
---

# GitHub Workflow Instructions

This file contains team standards for GitHub operations including PR workflows, branch management, commit conventions, and issue tracking.

## 0. Engineering Principles

- We design and operate with 12 Factor Application principles.
- We prefer simple, elegant, DRY implementations that are easy to change.
- We apply Gang of Four design patterns when they improve clarity and maintainability.
- We use BDD with Gherkin examples from unit tests through end-to-end tests.
- We document implementation intent in `CHANGE.md` before making code or file changes.
- We capture lasting decisions and direction in `LESSONS.md`.
- We document code and files in first person plural and explain a journey that junior contributors can follow.
- We keep our written tone informal, practical, and lightly humorous.

## 1. Branch Management

### Branch Naming Conventions

- **Feature branches** (from `develop`): `feature/ISSUE-XXX-description`
- **Bug fix branches** (from `develop`): `fix/ISSUE-XXX-description` or `bugfix/ISSUE-XXX-description`
- **Release branches** (from `develop`): `release/X.Y.Z`
- **Hotfix branches** (from `main`): `hotfix/ISSUE-XXX-description`
- **Documentation branches** (from `develop`): `docs/ISSUE-XXX-description`

**Example**: `feature/SYNC-1234-add-cosmos-db-support`

### Git Flow Rules

- We follow Git Flow.
- We do not commit directly to `main` or `develop`.
- All changes are made on topic branches and merged by pull request.
- Features and fixes merge into `develop`.
- Releases merge into `main` and are back-merged into `develop`.
- Hotfixes start from `main`, merge into `main`, and are back-merged into `develop`.

### Branch Protection Rules

- Main branches (`main`, `master`, `develop`) should have:
  - Require pull request reviews before merging
  - Require status checks to pass
   - Restrict direct pushes
  - Dismiss stale pull request approvals
  - Require branches to be up to date before merging

## 2. Pull Request (PR) Workflow

### PR Title Format

Use conventional commit style:
```
[TYPE] ISSUE-XXX: Brief description (50 chars max)
```

**Types**: `feat`, `fix`, `docs`, `refactor`, `test`, `ci`, `chore`, `perf`

**Example**: `[feat] SYNC-1234: Add hierarchical partition key support`

### PR Description Template

```markdown
## Description
Brief explanation of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Breaking change
- [ ] Performance improvement

## Related Issues
Closes #ISSUE-XXX

## How to Test
Steps to verify the changes work as expected

## Checklist
- [ ] Code follows project style guide
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Performance impact reviewed (if applicable)
```

### Code Review Standards

1. **Minimum Reviewers**: 1-2 approvals required (configurable per branch)
2. **Review Focus**:
   - Code correctness and logic
   - Test coverage (aim for 80%+)
   - BDD coverage using Gherkin scenarios across test layers
   - Performance implications
   - Security considerations
   - Documentation clarity
   - Adherence to conventions

3. **Comments**:
   - Use "Request changes" only for blocking issues
   - Use "Comment" for suggestions
   - Resolve threads only after addressing feedback

### Merging Strategy

- **Fast-forward merge** for linear history on feature branches
- **Squash merge** for multiple commits on same feature
- **Create merge commit** for release/hotfix branches for traceability
- **Delete branch** after merging

## 3. Commit Message Conventions

### Format

```
[TYPE] SCOPE: Subject (50 chars max)

Body (72 chars wrap, detailed explanation if needed)

Footer: ISSUE-XXX, BREAKING CHANGE notes
```

### Examples

```
[feat] cosmos: Add hierarchical partition key support

Implements HPK strategy to overcome 20GB partition limit.
Improves query flexibility for multi-partition reads.

Footer: SYNC-1234, closes #1234
```

```
[fix] sync: Handle 429 rate limit with exponential backoff

Retry logic now respects Retry-After header from service.

Footer: SYNC-1240
```

## 4. Issue Tracking

### Issue Labels

- `bug`: Defect in existing functionality
- `feature`: New capability or enhancement
- `documentation`: Docs/comments improvements
- `performance`: Speed/efficiency improvements
- `security`: Security-related issue
- `help wanted`: Community contribution welcome
- `good first issue`: Good for new contributors
- `blocked`: Waiting on external dependency
- `high`, `medium`, `low`: Priority levels

### Issue Template

```markdown
## Description
Clear, concise explanation of the issue

## Steps to Reproduce
1. Step one
2. Step two
3. Expected result
4. Actual result

## Environment
- OS: 
- Version:
- Relevant SDK/Framework versions

## Possible Solution
(Optional) Suggested approach to fix
```

## 5. GitHub Actions & CI/CD

### Expected Checks

- Code build succeeds
- Unit, integration, and end-to-end tests pass (with coverage reports)
- Gherkin scenarios are validated in CI where applicable
- Linting passes (ESLint, Checkstyle, etc.)
- Security scanning completes (SAST/DAST)
- Documentation builds successfully

### Workflow Files Location

- `.github/workflows/` — CI/CD pipeline definitions
- Each workflow should be named descriptively: `build-and-test.yml`, `security-scan.yml`

## 6. Release Management

### Version Format

Follow [Semantic Versioning](https://semver.org/):
- `MAJOR.MINOR.PATCH` (e.g., `1.2.3`)
- Pre-release: `1.2.3-alpha`, `1.2.3-beta.1`
- Build metadata: `1.2.3+build.123`

### Release Checklist

- [ ] Bump version in all relevant files
- [ ] Update CHANGELOG.md
- [ ] Tag commit with version: `git tag v1.2.3`
- [ ] Create GitHub Release with release notes
- [ ] Verify all artifacts published to registry
- [ ] Announce release in team channels

## 7. Security & Access Control

### Repository Secrets

- Store API keys, credentials in GitHub Secrets, not in code
- Use organization-level secrets for shared access
- Rotate secrets regularly
- Document secret names and usage in team wiki

### CODEOWNERS

- Maintain `.github/CODEOWNERS` file
- Designate code owners for critical paths
- Ensures proper review before merge

## 8. Local Overrides

**For team-specific or local-only customizations**, create `.github/local.instructions.md` (gitignored).

This allows:
- Local convention extensions
- Team-specific automation notes
- Experimental workflow guidance
- Environment-specific procedures

**Note**: Local overrides should NOT contradict this file; they enhance it.

## 9. Documentation Style

- We write in first person plural (we, our).
- We explain decisions and implementation as a journey junior contributors can follow.
- We prefer concise, practical explanations with a light touch of humor.
- We avoid unexplained jargon and include short examples for non-obvious behavior.

## 10. Design and Testing Expectations

- We align implementation and operations with 12 Factor Application guidance.
- We favor proven Gang of Four patterns when they reduce complexity.
- We define behavior with BDD examples and keep test language aligned from unit tests to end-to-end tests.
- We treat simplicity and DRY as merge-blocking quality signals, not optional polish.

## 11. Decision and Change Logging

- Before implementation, we add a short planned entry to `CHANGE.md`.
- After implementation, we update `CHANGE.md` with the completed result.
- We capture durable technical decisions in `LESSONS.md` so future contributors can understand intent quickly.

---

For questions or updates to these guidelines, contact the team leads or create a discussion in GitHub.

# GitHub Instructions Setup

This directory contains standardized GitHub workflow guidelines for the vdi-syncweave project.

## Team Baseline

- We follow 12 Factor Application principles.
- We use Gang of Four design patterns when they make code easier to evolve.
- We use BDD (Gherkin) from unit testing through end-to-end testing.
- We follow Git Flow and do not commit directly to `main` or `develop`.
- We favor elegant, simple, DRY changes.
- We write planned changes to `CHANGE.md` before implementation.
- We store decisions and direction in `LESSONS.md`.
- We document in first person plural, as a journey junior contributors can follow, with an informal tone and light humor.

## Files Overview

### Tracked Files (Committed to Repository)

- **`.github/INSTRUCTIONS.md`** ⭐ Main tracked file
  - Team-wide GitHub workflow standards
  - Branch naming conventions
  - PR workflow and review standards
  - Commit message conventions
  - Issue tracking guidelines
  - Release management procedures
  - **Never edit locally without pushing changes**

- **`.github/CODEOWNERS`** (if present)
  - Designates code owners for review routing
  - Ensures proper approval from domain experts

- **`.github/workflows/`**
  - CI/CD pipeline configurations
  - GitHub Actions automation
  - Status check definitions

### Gitignored Files (Local-Only)

- **`.github/local.instructions.md`** (gitignored)
  - Team-specific customizations
  - Machine-specific procedures
  - Local automation notes
  - **NOT committed to repository**
  - Edit freely for local use

- **`.vscode/copilot-instructions.md`** (gitignored)
  - AI assistant guidance for GitHub operations
  - Project-specific context and conventions
  - Copilot behavior customization

## Usage Guide

### For Individual Contributors

1. **Before creating a PR**:
   - Add a short planned entry to `CHANGE.md` before making changes
   - Read `.github/INSTRUCTIONS.md` for naming/format standards
   - Create a Git Flow branch from the correct base (feature/fix from `develop`, hotfix from `main`)
   - Check issue labels and templates

2. **When reviewing PRs**:
   - Refer to the Code Review Standards section
   - Use the PR description template provided
   - Verify all checks pass before approval, including BDD-oriented test coverage where applicable

3. **After implementation**:
   - Update `CHANGE.md` with completed outcomes
   - Record durable decisions in `LESSONS.md`

4. **For local customizations**:
   - Edit `.github/local.instructions.md` for team-specific workflow notes
   - This file is gitignored and won't be committed

### For Team Leads / Maintainers

1. **Update guidelines**:
   - Edit `.github/INSTRUCTIONS.md` directly
   - Submit as PR for review and approval
   - All team members should sync after merge

2. **Communicate changes**:
   - Announce breaking changes in team channels
   - Provide migration path for existing open PRs

3. **Local overrides**:
   - Use `.github/local.instructions.md` for experimental workflows
   - Once validated, propose as PR to main guidelines

### For Copilot / AI Assistants

- Uses `.vscode/copilot-instructions.md` for context
- Knows about Azure Cosmos DB best practices
- Follows project-specific conventions (SYNC-XXXX issues, Java/Python focus)
- Respects branch naming and commit conventions

## Gitignore Pattern

The following are blocked from version control:

```
# GitHub Workflow — Local Customizations
.github/local.instructions.md
**/.local.instructions.md

# VS Code Customizations
.vscode/settings.json
.vscode/launch.json
.vscode/tasks.json
.vscode/copilot-instructions.md
.vscode/*.code-workspace
.devcontainer/local*
```

## Workflow Examples

### Creating a Feature Branch

```bash
# Start from develop (Git Flow)
git checkout develop
git pull origin develop

# Create feature branch following convention
git checkout -b feature/SYNC-1234-add-cosmos-query-optimization

# Commit with convention
git commit -m "[feat] SYNC-1234: Optimize Cosmos DB partition key strategy

Implement hierarchical partition keys to handle larger datasets.
Reduces cross-partition query overhead by 40%.

Closes #1234"
```

### Opening a PR

1. Push feature branch: `git push origin feature/SYNC-1234-add-cosmos-query-optimization`
2. Open PR to `develop` on GitHub with title: `[feat] SYNC-1234: Add Cosmos query optimization`
3. Fill PR description using template from `.github/INSTRUCTIONS.md`
4. Request reviewers (minimum 1-2 approvals)
5. Ensure all status checks pass

### Merging PR

- Merge feature and fix branches into `develop`
- Merge release branches into `main`, then back-merge into `develop`
- Merge hotfix branches into `main`, then back-merge into `develop`
- Delete branch after merge

## Local Customization Example

Edit `.github/local.instructions.md` to add team-specific notes:

```markdown
# Local Team Customizations

## Our Sprint Review Schedule
- Every other Thursday at 2 PM
- PR approval window: 24 hours before review

## Performance Benchmarks
- Target P95 latency for API calls: <100ms
- Cosmos DB RU budget per operation: <5 RUs

## Security Review Checklist
- [ ] No credentials in code
- [ ] Input validation on all APIs
- [ ] Rate limiting configured
```

## Questions?

- Check `.github/INSTRUCTIONS.md` for detailed guidelines
- Create a GitHub Discussion for workflow improvements
- Contact team leads for clarification

---

**Last Updated**: 2026-09-05  
**Maintained By**: GitHub Workflow Task Force

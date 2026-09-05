# Deployment Guide

We deploy with boring consistency so production stays calm and our weekends stay mostly weekend-shaped.

## 1. Deployment Principles

- We follow Git Flow for all promotions.
- We do not deploy unreviewed branch code directly.
- We separate build, release, and run responsibilities.
- We keep configuration external and environment-specific.
- We design rollback as a first-class path, not an emergency invention.

## 2. Branch to Environment Flow

- Feature and fix branches target develop.
- develop is our integration branch and feeds release branches.
- Release branches promote to main after validation.
- Hotfix branches start from main, then merge back to both main and develop.

## 3. Release Preparation Checklist

Before creating a release branch:

- Confirm all required pull request checks are green.
- Confirm BDD scenarios for changed behavior are updated.
- Confirm CHANGE.md includes planned and completed entries.
- Confirm LESSONS.md captures durable decisions from this release cycle.
- Confirm no direct commits landed on main or develop.

## 4. Build and Artifact Expectations

- Build artifacts must be reproducible from tagged source.
- Release metadata should include commit, branch, and version.
- We keep artifact naming predictable and searchable.

## 5. Environment Configuration

- We externalize secrets and credentials.
- We externalize service endpoints and region-specific settings.
- We avoid environment branching in code where configuration can handle the difference.

## 6. Validation Gates

Minimum gates before production promotion:

- Compile/build success.
- Unit test and integration test pass.
- End-to-end smoke scenarios pass.
- Lint and static analysis checks pass.
- Security dependency checks pass.

## 7. Rollback and Recovery

- We keep the previous stable release artifact available.
- We document rollback steps per deployment pipeline.
- We test rollback periodically on non-production environments.
- We verify post-rollback data integrity and service health.

## 8. Post-Deployment Verification

After deployment, we verify:

- Core synchronization workflows execute successfully.
- Error and retry logs remain within expected bounds.
- Throughput and latency are consistent with baseline.
- No critical alerts are newly firing.

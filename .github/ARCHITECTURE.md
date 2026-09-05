# Architecture Guide

We design with reliability, clarity, and changeability in mind. Our architecture should make the right behavior the default behavior.

## 1. Architecture Principles

- We follow 12 Factor Application principles for configuration, deployability, and operability.
- We favor simple boundaries and explicit contracts.
- We apply Gang of Four patterns when they reduce coupling or improve extensibility.
- We keep behavior traceable from requirement to BDD scenario to test evidence.

## 2. 12 Factor Application Alignment

### Config

- We store environment-specific settings outside code.
- We avoid hardcoded endpoints, credentials, and tenant-specific values.

### Backing Services

- We treat connectors and data stores as attached resources.
- We isolate integration code behind interfaces so local and remote services can be swapped.

### Build, Release, Run

- We keep build artifacts reproducible.
- We separate build concerns from runtime configuration.

### Logs

- We treat logs as event streams.
- We include correlation fields so sync journeys can be reconstructed.

### Processes and Concurrency

- We design stateless workers when possible.
- We externalize durable state and avoid implicit in-memory coupling.

## 3. GoF Patterns We Prefer

### Strategy

- We use Strategy for pluggable retry behavior, transformation rules, and connector-specific logic.

### Factory Method / Abstract Factory

- We use factories to create connector and pipeline components without leaking construction details.

### Template Method

- We use template methods for sync flow skeletons where steps vary by source or target.

### Adapter

- We use adapters to normalize third-party or legacy interfaces.

### Decorator

- We use decorators for cross-cutting behavior such as metrics, logging enrichment, and tracing.

### Observer

- We use observer-style event publication for sync lifecycle notifications.

## 4. Reliability Patterns

- We fail fast for invalid state and invalid configuration.
- We classify failures as transient or permanent.
- We retry transient failures with bounded exponential backoff and jitter.
- We set explicit timeouts for all network and external service operations.
- We include idempotency keys or equivalent guards where operations may be retried.

## 5. Concurrency and State

- We avoid shared mutable state across worker threads.
- We protect shared resources with clear synchronization boundaries.
- We model workflows so retries and re-entrancy do not corrupt state.
- We keep long-running operations observable with progress checkpoints.

## 6. BDD Traceability Model

We keep behavior linked across layers:

1. Requirement or decision in docs.
2. Gherkin scenario defining expected behavior.
3. Unit tests validating local logic.
4. Integration tests validating service interactions.
5. End-to-end tests validating whole sync journeys.

If a critical behavior has no scenario, we treat it as a coverage gap.

## 7. Architecture Review Checklist

Before merge, we verify:

- Did we keep dependencies moving inward toward domain logic?
- Did we introduce any hidden coupling?
- Did we add or update Gherkin scenarios for behavior changes?
- Did we improve reliability signals (logs, metrics, error context)?
- Did we keep the solution simple enough for junior contributors to reason about?

# Current Feature: Milestone 2 — Domain Model

*Last updated: 2026-09-20*

## Overview

Define the core domain model as immutable records and sealed types, establishing the
foundation for all business logic. Focus on type-safe state machines and exhaustive
pattern matching for reservation states.

## Specification

### Requirements
- [ ] `Reservation` sealed interface with state records: `Pending`, `Confirmed`, `Expired`, `Cancelled`
- [ ] State transition methods that return new instances (immutable)
- [ ] `InventoryItem` record with tenant ID, name, quantity fields
- [ ] `Tenant` record with ID, name, configuration fields
- [ ] Domain event records: `ReservationCreated`, `ReservationConfirmed`, `ReservationExpired`, `ReservationCancelled`
- [ ] Unit tests for all state transitions and validation

### Acceptance Criteria
- [ ] `mvn clean verify` passes with 100% test pass rate
- [ ] All domain events are immutable records
- [ ] State transitions use pattern matching in exhaustive switch expressions
- [ ] No mutable state or boolean flags in domain entities
- [ ] README example showing Reservation state machine usage

## Implementation Plan

1. **Define sealed `Reservation` hierarchy** in `domain-events/` module
   - Sealed interface `Reservation` with record implementations
   - Each state (`Pending`, `Confirmed`, etc.) is a record
   - Include methods for state transitions: `confirm()`, `expire()`, `cancel()`

2. **Define value objects** as records
   - `InventoryItem` record
   - `Tenant` record

3. **Define domain events** as records
   - Reuse in Milestone 6 for Kafka publishing
   - Include metadata: tenant ID, timestamp, correlation IDs

4. **Write unit tests** for domain model
   - Test state transitions (Pending → Confirmed, Pending → Expired, etc.)
   - Test invalid transitions are prevented at compile time (via sealed types)
   - Test pattern matching exhaustiveness

5. **Update README** with example code showing the model in action

## Testing Strategy

- Unit tests: domain/reservation/ReservationTest.java
  - Test each valid state transition
  - Verify immutability (no setters)
  - Verify pattern matching covers all states
- No integration tests needed (domain model has no dependencies)

## Files to Create/Modify

- `domain-events/src/main/java/dev/noe/loomcrete/domain/Reservation.java` (sealed interface)
- `domain-events/src/main/java/dev/noe/loomcrete/domain/reservation/Pending.java` (record)
- `domain-events/src/main/java/dev/noe/loomcrete/domain/reservation/Confirmed.java` (record)
- `domain-events/src/main/java/dev/noe/loomcrete/domain/reservation/Expired.java` (record)
- `domain-events/src/main/java/dev/noe/loomcrete/domain/reservation/Cancelled.java` (record)
- `domain-events/src/main/java/dev/noe/loomcrete/domain/InventoryItem.java` (record)
- `domain-events/src/main/java/dev/noe/loomcrete/domain/Tenant.java` (record)
- `domain-events/src/test/java/dev/noe/loomcrete/domain/ReservationTest.java` (unit tests)

## Status

- [ ] Spec approved
- [ ] Build started
- [ ] Tests passing
- [ ] Feature complete
- [ ] Code review passed
- [ ] Ready to merge

## Blockers

None yet.

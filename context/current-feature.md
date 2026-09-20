# Current Feature: Milestone 2 — Domain Model

*Last updated: 2026-09-20*

## Overview

Define the core domain model as immutable records and sealed types, establishing the
foundation for all business logic. Focus on type-safe state machines and exhaustive
pattern matching for reservation states.

## Specification

### Requirements
- [x] `Reservation` sealed interface with state records: `Pending`, `Confirmed`, `Expired`, `Cancelled`
- [x] State transition methods that return new instances (immutable)
- [x] `InventoryItem` record with tenant ID, name, quantity fields
- [x] `Tenant` record with ID, name, configuration fields
- [x] Domain event records: `ReservationCreated`, `ReservationConfirmed`, `ReservationExpired`, `ReservationCancelled`
- [x] Unit tests for all state transitions and validation

### Acceptance Criteria
- [x] `mvn clean verify` passes with 100% test pass rate
- [x] All domain events are immutable records
- [x] State transitions use pattern matching in exhaustive switch expressions
- [x] No mutable state or boolean flags in domain entities
- [ ] README example showing Reservation state machine usage (defer to Polish milestone)

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

- [x] Spec approved
- [x] Build started
- [x] Tests passing (24 tests, all passing)
- [x] Feature complete
- [ ] Code review passed
- [x] Ready to merge

## Implementation Summary

**Domain Model Classes Created:**
- `Reservation` sealed interface with 4 record implementations
  - `Pending` — initial state, can transition to Confirmed, Expired, or Cancelled
  - `Confirmed` — reservation accepted, cannot revert
  - `Expired` — reservation timed out, can be cancelled
  - `Cancelled` — reservation cancelled, terminal state
- `InventoryItem` record with reserve/release methods for quantity management
- `Tenant` record for multi-tenancy context

**Domain Events (4 records):**
- `ReservationCreated`, `ReservationConfirmed`, `ReservationExpired`, `ReservationCancelled`

**Tests (24 total):**
- `ReservationTest` — 14 tests covering state transitions, validation, pattern matching
- `InventoryItemTest` — 8 tests covering reserve/release, validation, immutability
- `TenantTest` — 2 tests covering creation and immutability

**Key Features:**
- Type-safe state machine via sealed types (impossible to create invalid states)
- Immutable records with no setters
- Exhaustive pattern matching in switch expressions
- Comprehensive validation in compact constructors
- Natural language state transition methods (confirm, expire, cancel)

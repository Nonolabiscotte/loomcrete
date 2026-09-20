# Completed Feature: Milestone 2 — Domain Model

*Completed: 2026-09-20*

## Overview

Define the core domain model as immutable records and sealed types, establishing the
foundation for all business logic. Focus on type-safe state machines and exhaustive
pattern matching for reservation states.

## Implementation Summary

**Domain Model Classes Created:**
- `Reservation` sealed interface in `dev.noe.loomcrete.domain.reservations` with 4 record implementations
  - `Pending` — initial state, can transition to Confirmed, Expired, or Cancelled
  - `Confirmed` — reservation accepted, cannot revert
  - `Expired` — reservation timed out, can be cancelled
  - `Cancelled` — reservation cancelled, terminal state
- `InventoryItem` record with reserve/release methods for quantity management
- `Tenant` record for multi-tenancy context

**Domain Events:**
- Consolidated into single `ReservationEvent` record with `ReservationEventType` enum
- Factory methods: `.created()`, `.confirmed()`, `.expired()`, `.cancelled()`
- Eliminates code duplication from 4 identical record definitions

**Value Objects:**
- All implemented as immutable records with validation in compact constructors

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
- One class per file (SRP) with dedicated `reservations` package

## Architecture

```
domain/
├── Tenant.java (record)
├── InventoryItem.java (record)
├── events/
│   ├── ReservationEvent.java (record)
│   └── ReservationEventType.java (enum)
└── reservations/
    ├── Reservation.java (sealed interface)
    ├── Pending.java (record)
    ├── Confirmed.java (record)
    ├── Expired.java (record)
    └── Cancelled.java (record)
```

## Status

- [x] All requirements met
- [x] All acceptance criteria met
- [x] 24 tests passing
- [x] Code merged to main
- [x] Ready for Milestone 3 (Persistence)

## Design Decisions

1. **Sealed interfaces over enums** for Reservation states to enable type-safe transitions
2. **One record per file** to follow single responsibility principle
3. **Consolidated events** to eliminate duplication and simplify event handling
4. **Immutable records** with comprehensive validation via compact constructors

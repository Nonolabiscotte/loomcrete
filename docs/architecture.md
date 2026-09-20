# Architecture

loomcrete follows **onion architecture** (hexagonal architecture principles) to
keep domain logic isolated from framework details and enable testing.

## Onion Layers

### 1. Domain (innermost, no dependencies)
Business logic and entities — zero framework/library dependencies.

- **`Reservation`** — sealed hierarchy of immutable records: `Pending`, `Confirmed`,
  `Expired`, `Cancelled` with state transition logic via pattern matching.
- **`InventoryItem`** — unit of inventory with available quantity, owned by a tenant.
- **`Tenant`** — multi-tenancy boundary; every request is scoped to a tenant ID.
- **Domain events** — immutable records: `ReservationCreated`, `ReservationConfirmed`,
  `ReservationExpired`, `ReservationCancelled` emitted on state transitions.

### 2. Application (use cases and orchestration)
Orchestrates domain logic and infrastructure to implement business flows. Contains
no domain logic of its own, only sequencing and coordination.

- **`ReservationService`** — orchestrates reservation creation, structured-concurrency
  checks (inventory availability, pricing, fraud detection), and state transitions.
- **`InventoryService`** — inventory CRUD and availability queries.
- **`TenantService`** — multi-tenancy context and isolation.

### 3. Infrastructure (adapters and persistence)
Bridges between application services and external systems (database, messaging,
caching). Implements repository interfaces and event publishers.

- **Panache Repositories** — `ReservationRepository`, `InventoryRepository`,
  blocking JDBC queries dispatched onto virtual threads.
- **Kafka Publisher** — publishes domain events to Kafka topics via SmallRye
  Reactive Messaging.
- **Dev Services** — Postgres and Kafka containers for local dev and tests.
- **Quarkus Caching** — optional Redis or in-memory cache for hot inventory reads.

### 4. Presentation/API (outermost, frameworks)
HTTP REST layer — converts HTTP requests/responses to/from domain/application
concepts.

- **REST Controllers** — `ReservationController`, `InventoryController`, etc.
- **DTOs** — request/response objects, mapped to/from domain entities.
- **Error handling** — HTTP status codes and error response formatting.
- **OpenAPI/Swagger** — auto-generated API documentation.

## Key Architectural Decisions

### Decision 1: Blocking JDBC on Virtual Threads
- **What:** Use traditional Panache (blocking JDBC), not reactive Hibernate Reactive.
  Quarkus automatically dispatches blocking calls onto virtual threads.
- **Why:** Virtual threads + blocking persistence demonstrate the virtual-threads-native
  alternative to reactive, and allow `StructuredTaskScope` to shine in the
  structured-concurrency milestone without requiring Mutiny combinators.
- **Trade-offs:** Blocks I/O in worker code (but on cheap virtual threads), not
  zero-copy reactive. Reflection in JDBC doesn't go away, but is acceptable at
  this scale.
- **When:** Decided 2026-09-20 after reviewing the original reactive stack.

### Decision 2: Domain Events as Immutable Records
- **What:** Domain events (`ReservationCreated`, etc.) are immutable records,
  published to Kafka whenever state transitions occur.
- **Why:** Events-first design enables audit trails, eventual consistency, and
  decoupling of services in later stages. Records are lightweight and integrate
  with Java 21 pattern matching.
- **Trade-offs:** Requires event publishers in infrastructure; adds a Kafka
  dependency early. Mitigated by Dev Services.
- **When:** Original design in CLAUDE.md.

### Decision 3: Sealed Interfaces for Reservation State
- **What:** `Reservation` is a sealed interface with record implementations
  (`Pending`, `Confirmed`, etc.), not a single entity with mutable status fields.
- **Why:** Type-safe state machine via pattern matching; impossible to create
  invalid state combinations. Enforces exhaustive handling in switch expressions.
- **Trade-offs:** More boilerplate than a single entity (but records minimize it).
  Panache entity mapping requires care (joins/inheritance).
- **When:** Original design in CLAUDE.md.

## Component Interactions

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation/API                      │
│        REST Controllers ← DTOs → HTTP Responses         │
└────────────────────────┬────────────────────────────────┘
                         │ (request/response)
┌────────────────────────▼────────────────────────────────┐
│               Application Services                      │
│  ReservationService, InventoryService (orchestration)  │
└────────────────────────┬────────────────────────────────┘
                         │ (commands, state transitions)
┌────────────────────────▼────────────────────────────────┐
│                    Domain                              │
│  Reservation (sealed), InventoryItem, Tenant,          │
│  Domain Events (immutable records)                     │
└────────────────────────┬────────────────────────────────┘
                         │ (domain events, queries)
┌────────────────────────▼────────────────────────────────┐
│                Infrastructure                          │
│  Panache Repos (JDBC) ← Kafka Publisher ← Domain Events│
│  Dev Services: Postgres, Kafka                         │
└─────────────────────────────────────────────────────────┘
```

## Data Flow

### Reservation Creation
1. **API** receives POST `/reservations` with tenant ID and reservation details.
2. **ReservationService** orchestrates:
   - Structured-concurrency checks: inventory available? pricing OK? fraud-check pass?
   - Create domain `Reservation` (initially `Pending`).
   - Save via `ReservationRepository` (Panache).
3. On success, **`ReservationCreated`** domain event emitted.
4. **Kafka Publisher** publishes event to `reservation-events` topic.
5. **Response** returns `201 Created` with confirmation details.

### Background Expiry
1. **Scheduled task** (virtual-thread-backed) runs periodically.
2. Queries all `Pending` reservations older than TTL via `ReservationRepository`.
3. Transitions each to `Expired`, saves back via Panache.
4. **`ReservationExpired`** events published to Kafka.
5. External consumers consume and log/record event details.

## Testing Strategy

- **Domain** (no mocks): unit tests for `Reservation` state transitions, `InventoryItem` queries via pattern matching.
- **Application** (mock infrastructure): unit tests for `ReservationService` orchestration logic; mock repositories and event publishers.
- **Infrastructure** (with Dev Services): `@QuarkusTest` integration tests for Panache queries, Kafka publishing; Postgres/Kafka containers spun up automatically.
- **Presentation** (with mocks): REST controller tests; mock application services.

## Future Considerations

- **Caching layer** (Quarkus Cache or Redis) for hot inventory reads (milestone 3+).
- **Event sourcing** — consider storing immutable events as the source of truth instead of snapshotting state (future refinement, not in initial scope).
- **Multi-tenancy isolation** — currently scoped per-request; could add database-level row-level security.
- **Error handling & validation** — currently basic; add structured error codes and validation library in Polish milestone.

# CLAUDE.md — Loomcrete

This file is project memory for Claude Code (or any Claude instance working in this
repository). Read it fully before making changes. It describes what the project is,
how it's built, and the working style expected on every task.

## What this project is

**Loomcrete** is a learning project: a small but realistic **multi-tenant
reservation/inventory service**, built specifically to exercise Java 21 and modern
Quarkus features that day-to-day CRUD work often doesn't touch. The business domain
(reserving units of inventory — think seats, storage slots, rental units) is
intentionally simple and incidental to the project's name and purpose. The point is
the engineering: virtual threads, structured concurrency, sealed types/pattern
matching, reactive persistence, event-driven messaging, and native compilation.

Owner: Noé, senior backend Java engineer (Java/Quarkus, Spring Boot, Docker,
Kubernetes, Azure in his day job). This project exists to deepen Java 21 / Quarkus
skills for interviews and personal growth — code quality and clear rationale matter
more than shipping speed.

## Tech stack

- Java 21 (LTS) — virtual threads, structured concurrency (preview API, via
  `--enable-preview` where needed), records, sealed interfaces, pattern matching
  for switch, sequenced collections.
- Quarkus (latest stable) with:
  - RESTEasy Reactive for the HTTP layer
  - Blocking JDBC + Hibernate ORM with Panache for persistence, dispatched onto
    virtual threads to avoid blocking the I/O executor (the core advantage of
    virtual threads + blocking persistence over reactive persistence)
  - SmallRye Reactive Messaging (Kafka connector) for domain events
  - SmallRye Fault Tolerance for resilience (circuit breaker / retry) once we reach
    that stage
  - Quarkus Cache (or Redis client) for hot inventory reads — later stage
  - Quarkus Dev Services + Testcontainers for integration tests (Postgres, Kafka)
- Build tool: Maven (unless the user says otherwise).
- GraalVM native image build as a later milestone, with lightweight native smoke-build
  checkpoints after major stack additions (persistence, Kafka) to catch reflection/
  serialization issues incrementally, and a final measurement milestone comparing
  JVM vs native startup time / memory footprint.

## Domain model (initial cut — refine as we go, don't over-design upfront)

- `Reservation` — sealed hierarchy of states as sealed interfaces/records:
  `Pending`, `Confirmed`, `Expired`, `Cancelled`. Model transitions with pattern
  matching in switch expressions, not boolean flags or a mutable status enum.
- `InventoryItem` — a unit of inventory belonging to a tenant, with an available
  quantity.
- `Tenant` — simple multi-tenancy: every request is scoped to a tenant ID.
- Domain events (records): `ReservationCreated`, `ReservationConfirmed`,
  `ReservationExpired`, `ReservationCancelled` — published to Kafka via Reactive
  Messaging whenever a reservation transitions state.

## Core technical scenarios to build toward

1. **Virtual threads** for request handling and persistence layer so the service
   handles high concurrent load without manual thread-pool tuning. Blocking JDBC
   Panache calls are dispatched onto virtual threads by design (not an exception)
   to demonstrate the virtual-threads-native alternative to reactive persistence.
2. **Structured concurrency** (built on top of virtual threads) to fan out a
   "create reservation" request into parallel sub-checks (inventory availability,
   pricing, a simulated fraud/rate-limit check) that succeed or fail together
   using `StructuredTaskScope`, with proper cancellation propagation if one check fails.
3. A **background expiry job** (virtual-thread-backed scheduled task) that sweeps
   stale `Pending` reservations to `Expired` and emits the corresponding event.
4. An **event log/outbox** per tenant, using sequenced collections where a defined
   iteration/insertion order matters.
5. **Native image** build with incremental smoke-build checkpoints (after persistence,
   after Kafka) and a final measurement milestone showing cold start time and RSS
   memory comparison vs the JVM build.

## Working style — READ THIS BEFORE WRITING CODE

**Always implement step by step. Never jump ahead to later milestones.**

### Implementation Planning & Scope

**IMPORTANT:** At the start of every implementation task:

1. **Check `context/current-feature.md`** — it is the source of truth for the active
   milestone's requirements, acceptance criteria, and implementation plan. Follow it.
2. **Create a humanly-scoped implementation plan** before writing any code:
   - Break the milestone into focused steps small enough to code and test in 15–30 minutes
   - Each step should be reviewable, testable, and self-contained
   - Explain *why* each step matters and *what* code change it introduces
   - Aim for clarity: a reader should understand the problem being solved, not just the code
3. **Present the plan to the user** for approval. Do not start coding until approved.
4. **Execute step-by-step**: code one step, test it, wait for user approval, then move to
   the next step. Do not batch multiple steps in a single turn unless the user asks.
5. **Report results after each step**: build status, test results, blockers, what changed.
   Stop for feedback before proceeding.

### General Principles

- Work through the milestones in order. Do not start milestone N+1 until milestone N
  compiles, has passing tests, and has been explicitly confirmed by the user.
- Each milestone should be small enough to review in one sitting. If it feels big,
  propose splitting it before starting.
- Commit at the end of each completed milestone with a clear, conventional commit message.
  One milestone = one (or a few) focused commits, not one giant commit at the end.
- Prefer explaining *why* a Java 21/Quarkus feature is used in a given spot over
  just using it — this project's goal is understanding, not just working code.
- When there's a genuine design choice (e.g., how to model reservation expiry,
  which Kafka topic layout to use), present the tradeoff briefly and ask, rather
  than silently picking one and moving on — unless the choice is truly minor.
- Keep dependencies minimal. Don't add a library "for later" — add it when the
  milestone that needs it arrives.
- Tests are not optional. Each milestone that adds behavior should add or update
  tests for that behavior (unit tests for domain logic, `@QuarkusTest` /
  Dev Services-backed integration tests for the REST/persistence/messaging layers).

## Completed milestones

1. **✅ Bootstrap** (commit `86ec1ae`)
   - Quarkus project skeleton with health check endpoint, project structure, CI-friendly build
2. **✅ Domain model** (commit `171e5cd`)
   - Sealed `Reservation` hierarchy, `InventoryItem`, `Tenant` as records with unit tests
3. **✅ Persistence** (commit `e7508ac`)
   - Blocking JDBC Panache repositories for all three services (TenantRepository, InventoryItemRepository, ReservationRepository)
   - JPA entities with multi-tenancy support (indexes on tenant_id, status)
   - Dev Services configuration (PostgreSQL auto-start on `mvn quarkus:dev`)
   - 52 integration tests passing (5 Tenant + 7 Inventory + 12 Reservation + 28 domain model tests)
   - Sealed type conversion using Java 21 pattern matching (Reservation entity ↔ domain types)
4. **✅ Virtual threads** (commit in progress)
   - Service layer (TenantService, InventoryService, ReservationService) using blocking repository calls
   - Verified Quarkus 3.8.6 automatically dispatches blocking JDBC onto virtual threads
   - Added "Virtual Threads & Blocking JDBC" documentation to README
   - All 52 tests passing on virtual threads (no explicit `@RunOnVirtualThread` needed)
   - Foundation for structured concurrency in Milestone 5

## Suggested milestones (remaining)

1. **Bootstrap**: Quarkus project skeleton named `loomcrete`, health check
   endpoint, project structure, CI-friendly build (`mvn verify` passes), this
   CLAUDE.md and a basic README.
2. **Domain model**: sealed `Reservation` hierarchy, `InventoryItem`, `Tenant` as
   records, with unit tests for state transitions via pattern matching.
3. **Persistence**: blocking JDBC Panache repositories, Dev Services wired for
   local dev and tests, basic CRUD for inventory and reservations.
4. **Virtual threads**: apply `@RunOnVirtualThread` to the blocking persistence
   layer, verify JDBC calls are dispatched onto virtual threads, short load test
   showing request throughput vs platform threads.
5. **Reservation creation flow with structured concurrency**: REST endpoint that
   uses `StructuredTaskScope` to fan out the parallel checks (inventory availability,
   pricing, fraud/rate-limit check) described above, returns `Confirmed` or a
   clear failure reason.
6. **Domain events over Kafka**: publish events on every state transition,
   consumer(s) that at minimum log/record them. *(Native smoke-build checkpoint:*
   *verify GraalVM native image still builds after Kafka integration.)*
7. **Expiry job**: scheduled virtual-thread-backed sweep of stale reservations.
8. **Resilience**: circuit breaker / retry on a chosen external-dependency-like
   call using SmallRye Fault Tolerance.
9. **Native image**: build for GraalVM, measure cold start time and RSS memory,
   write comparison vs JVM build. *(Incremental smoke builds after milestone 3*
   *already de-risked most reflection/serialization issues.)*
10. **Polish**: OpenAPI docs, structured logging, README with architecture
    diagram and how-to-run instructions.

## Conventions

- Package root: `dev.noe.loomcrete` (adjust if the user prefers something else —
  confirm on bootstrap, don't assume).
- Records for all DTOs and immutable domain data. No Lombok — Java 21 records and
  pattern matching remove most of the need for it, and the point of this project is
  to use the language directly.
- Sealed interfaces + exhaustive switch for anything resembling a state machine.
- No global mutable state; favor constructor injection.
- Write commit messages and code comments in English.

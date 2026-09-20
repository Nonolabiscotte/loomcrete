# Roadmap

Progress tracking for loomcrete development.

## Milestones

- [ ] 1. **Bootstrap** — Quarkus project skeleton, health check endpoint, project structure, CI-friendly build
- [ ] 2. **Domain model** — sealed `Reservation` hierarchy, `InventoryItem`, `Tenant` records, unit tests
- [ ] 3. **Persistence** — blocking JDBC Panache repositories, Dev Services, basic CRUD
- [ ] 4. **Virtual threads** — apply `@RunOnVirtualThread` to persistence layer, load test vs platform threads
- [ ] 5. **Reservation creation flow with structured concurrency** — REST endpoint using `StructuredTaskScope`
- [ ] 6. **Domain events over Kafka** — publish events on state transitions, consumer logging
- [ ] 7. **Expiry job** — scheduled virtual-thread-backed sweep of stale reservations
- [ ] 8. **Resilience** — circuit breaker / retry using SmallRye Fault Tolerance
- [ ] 9. **Native image** — GraalVM build, measure cold start and RSS memory, JVM vs native comparison
- [ ] 10. **Polish** — OpenAPI docs, structured logging, README with architecture diagram

## Recently Completed

See `context/completed/` for detailed feature documentation.

## Current Focus

See `context/current-feature.md` for the active feature being developed.

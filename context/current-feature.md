# Current Feature: Milestone 4 — Virtual Threads

*Last updated: 2026-09-21*

## Overview

Apply `@RunOnVirtualThread` to blocking persistence operations, verifying that JDBC calls are
dispatched onto virtual threads instead of platform threads. This unlocks the core advantage of
Java 21 virtual threads: high concurrent load without manual thread-pool tuning.

## Specification

### Requirements
- [ ] Apply `@RunOnVirtualThread` annotation to repository query/persist methods or service layer
- [ ] Verify JDBC calls execute on virtual threads (via logging/tracing)
- [ ] Confirm dev mode works: `mvn quarkus:dev` without blocking issues
- [ ] Short load test: demonstrate throughput improvement vs platform threads (optional)
- [ ] Document virtual thread configuration and behavior in README

### Acceptance Criteria
- [ ] `mvn clean verify` passes
- [ ] All integration tests pass with virtual thread dispatcher
- [ ] No deadlocks or thread starvation observed
- [ ] Load test (if run) shows improved throughput or same throughput with lower footprint

## Implementation Plan

1. **Understand virtual thread integration**
   - Quarkus automatically dispatches blocking Panache/JDBC onto virtual threads when enabled
   - `@RunOnVirtualThread` can be applied at REST endpoint or service layer
   - No code changes to repositories needed (just verification/documentation)

2. **Enable and verify virtual thread dispatch**
   - Check Quarkus config for virtual thread dispatcher (may be default in 3.8.6)
   - Run integration tests and confirm no blocking issues
   - Optional: add logging to show virtual thread IDs in test output

3. **Document the pattern**
   - Explain why: blocking I/O on virtual threads is efficient, not wasteful
   - Contrast with reactive persistence (more complex, not needed here)
   - Mention preview API usage if structured concurrency is added later

4. **Performance observation** (optional)
   - Run a load test: create/read reservations under concurrent load
   - Measure: throughput, latency, thread count
   - Document comparison if done

## Testing Strategy

- Unit tests: no changes (domain model unchanged)
- Integration tests: run existing @QuarkusTest tests, confirm no blocking
- Load test (optional): concurrent reservation creation/read, measure latency
- Dev mode: `mvn quarkus:dev`, interact with services, confirm no hangs

## Files to Create/Modify

**Per-service (if adding @RunOnVirtualThread):**
- REST endpoints or service layer (to be created in Milestone 5)
- application.yml (verify/document virtual thread config)

**Documentation:**
- README: add section explaining virtual thread usage and why it matters

## Status

- [x] Spec approved
- [x] Build started
- [ ] Tests passing (running...)
- [ ] Feature complete
- [ ] Code review passed
- [ ] Ready to merge

## Progress

**Step 1 (Complete):**
- Verified Quarkus 3.8.6 uses virtual threads by default for blocking JDBC
- Added "Virtual Threads & Blocking JDBC" section to README

**Step 2 (In Progress):**
- Created ReservationService with methods using blocking repository calls
- Created TenantService with similar pattern
- Created InventoryService with similar pattern
- Added thread logging to verify virtual thread execution (DEBUG level)
- All methods rely on Quarkus automatic dispatch to virtual threads (no explicit annotation needed)

## Blockers

None yet.

## Notes

- Virtual threads are stable in Java 21 LTS; no preview flag needed for basic usage
- Structured concurrency (`StructuredTaskScope`) is still preview (milestone 5+)
- Quarkus 3.8.6 handles virtual thread integration transparently for blocking I/O
- No code changes to repositories; just verification and documentation

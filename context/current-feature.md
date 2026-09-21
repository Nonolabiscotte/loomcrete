# Current Feature: Milestone 5 — Reservation Creation with Structured Concurrency

*Last updated: 2026-09-21*

## Overview

Build a REST endpoint for reservation creation that uses Java 21 structured concurrency (`StructuredTaskScope`)
to fan out parallel sub-checks (inventory availability, pricing, fraud/rate-limit detection) that succeed or fail
together with proper cancellation propagation. This demonstrates the power of virtual threads + structured concurrency
for high-load request handling.

## Specification

### Requirements
- [ ] REST endpoint: `POST /reservations` that creates a new reservation
- [ ] Parallel checks using `StructuredTaskScope`:
  - Inventory availability check (blocking JDBC via InventoryService)
  - Pricing/discount calculation (simulated logic)
  - Fraud/rate-limit check (simulated external call)
- [ ] Atomicity: all checks succeed or entire request fails (no partial states)
- [ ] Cancellation propagation: if one check fails, others are cancelled
- [ ] Request validation: tenant ID, inventory item ID, quantity validation
- [ ] Response: `Confirmed` reservation or error with clear failure reason

### Acceptance Criteria
- [ ] `mvn clean verify` passes with new REST tests
- [ ] Structured concurrency properly handles task cancellation
- [ ] All 3 sub-checks run in parallel (verified via timing/logs)
- [ ] No deadlocks or resource leaks
- [ ] Request/response properly serialized to JSON
- [ ] Error responses include meaningful failure reasons

## Implementation Plan

1. **Create ReservationResource REST controller**
   - `@Path("/reservations")` endpoint
   - `@POST` method accepting tenant ID, inventory item ID, quantity
   - Inject ReservationService, InventoryService, and create parallel check services

2. **Implement parallel checks with StructuredTaskScope**
   - `InventoryCheckTask` — verify availability using InventoryService.findAndReserve()
   - `PricingCheckTask` — calculate pricing (simulated logic)
   - `FraudCheckTask` — fraud/rate-limit check (simulated external call)
   - Use `StructuredTaskScope.ShutdownOnSuccess` to fail fast if any check fails

3. **Handle results and create reservation**
   - Combine check results
   - Create new Pending reservation via ReservationService
   - Return Confirmed response or error

4. **Add integration tests**
   - Success path: valid reservation with all checks passing
   - Failure paths: inventory unavailable, pricing check fails, fraud check fails
   - Verify parallel execution (timing test)
   - Verify cancellation propagation (one failing task cancels others)

5. **Document structured concurrency pattern**
   - Explain why: fan-out parallelism with guaranteed cancellation
   - Contrast with ExecutorService (manual cancellation, no guarantees)
   - Show how virtual threads make this efficient (cheap threads, no thread starvation)

## Testing Strategy

- Integration tests: `@QuarkusTest` with REST endpoints
  - Happy path: valid reservation → Confirmed
  - Failure paths: each check type fails independently
  - Timing test: verify parallel execution (3x faster than sequential)
  - Cancellation test: verify one failure cancels all tasks
- Load test (optional): concurrent reservation requests under `StructuredTaskScope`

## Files to Create/Modify

**Per-service:**
- `reservation-service/src/main/java/dev/noe/loomcrete/reservation/ReservationResource.java` (REST)
- `inventory-service/src/main/java/dev/noe/loomcrete/inventory/InventoryService.java` (enhance to support checks)
- `tenant-service/src/main/java/dev/noe/loomcrete/tenant/TenantService.java` (if needed for validation)

**Tests:**
- `reservation-service/src/test/java/dev/noe/loomcrete/reservation/ReservationResourceTest.java`

**Documentation:**
- README: add section on structured concurrency and reservation creation flow

## Status

- [ ] Spec approved
- [ ] Build started
- [ ] Tests passing
- [ ] Feature complete
- [ ] Code review passed
- [ ] Ready to merge

## Blockers

None yet.

## Notes

- `StructuredTaskScope` is a preview API; requires `--enable-preview`
- `StructuredTaskScope.ShutdownOnSuccess` cancels remaining tasks if one succeeds/fails
- Virtual threads make structured concurrency lightweight and efficient
- This is the first "business logic" milestone; previous ones were infrastructure

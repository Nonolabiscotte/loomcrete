# Completed Feature: Milestone 3 — Persistence

*Completed: 2026-09-21*

## Overview

Implement blocking JDBC + Hibernate ORM with Panache repositories for all three services,
establishing data access patterns that bridge the domain model to the database. Configure
Dev Services for automatic PostgreSQL container startup and create comprehensive integration
tests with multi-tenancy isolation and sealed type conversion.

## Implementation Summary

**JPA Entities Created:**
- `TenantEntity` in `tenant-service/infrastructure/` — maps domain `Tenant` record to database
- `InventoryItemEntity` in `inventory-service/infrastructure/` — maps domain `InventoryItem` record with indexes on `tenant_id`
- `ReservationEntity` in `reservation-service/infrastructure/` — maps sealed `Reservation` hierarchy to single entity with `status` column (PENDING, CONFIRMED, EXPIRED, CANCELLED)
- `ReservationStatus` enum for type-safe status representation

**Panache Repositories:**
- `TenantRepository extends PanacheRepositoryBase<TenantEntity, String>` — CRUD only
- `InventoryItemRepository extends PanacheRepositoryBase<InventoryItemEntity, String>` with `findByTenantId(tenantId)`
- `ReservationRepository extends PanacheRepositoryBase<ReservationEntity, String>` with:
  - `findByTenantId(tenantId)` — multi-tenancy isolation
  - `findByStatus(status)` — for scheduled expiry jobs
  - `findByTenantIdAndStatus(tenantId, status)` — precise combined queries

**Type Conversion (Java 21 Pattern Matching):**
- `ReservationEntity.from(Reservation)` — uses switch pattern matching to extract state-specific fields
- `ReservationEntity.toDomain()` — reconstructs correct sealed type (Pending/Confirmed/Expired/Cancelled) from status column

**Database Indexes:**
- Single-column: `idx_tenant_id`, `idx_status`
- Composite: `idx_tenant_status` on (tenant_id, status) for efficient tenant-scoped status queries

**Dev Services Configuration:**
- All services: `quarkus.devservices.enabled=true`
- PostgreSQL containers auto-start on `mvn quarkus:dev`
- Separate databases per service: loomcrete_tenant, loomcrete_inventory, loomcrete_reservation

**Test Infrastructure:**
- H2 in-memory database for integration tests (no Docker required)
- `src/test/resources/application.yml` per service with H2 configuration
- `quarkus-jdbc-h2` dependency for test scope

**Tests (52 total):**
- Domain model unit tests: 28 (Tenant, InventoryItem, Reservation states)
- TenantRepositoryTest: 5 tests (persist, retrieve, convert, update, delete, count)
- InventoryItemRepositoryTest: 7 tests (CRUD, multi-tenancy filtering, total quantity calculation)
- ReservationRepositoryTest: 12 tests (all 4 sealed types, status conversion, multi-tenancy, status queries, combined queries, state transitions)

## Architecture

```
tenant-service/infrastructure/
├── TenantEntity.java (JPA)
└── TenantRepository.java (Panache)
   └── test/TenantRepositoryTest.java

inventory-service/infrastructure/
├── InventoryItemEntity.java (JPA, @Index on tenant_id)
└── InventoryItemRepository.java (Panache with findByTenantId)
   └── test/InventoryItemRepositoryTest.java

reservation-service/infrastructure/
├── ReservationEntity.java (JPA, sealed type conversion via pattern matching)
├── ReservationStatus.java (Enum)
└── ReservationRepository.java (Panache with findByTenantId, findByStatus, combined)
   └── test/ReservationRepositoryTest.java
```

## Status

- [x] All requirements met
- [x] All acceptance criteria met
- [x] 52 tests passing (28 domain + 5 tenant + 7 inventory + 12 reservation)
- [x] Build verified: `mvn clean verify` ✅
- [x] Code merged to main (commit e7508ac)
- [x] Dev Services tested and working
- [x] Multi-tenancy isolation enforced at data access layer
- [x] Ready for Milestone 4 (Virtual Threads)

## Design Decisions

1. **Single status column for Reservation** (not TABLE_PER_CLASS inheritance)
   - Simpler queries: no join complexity, single table scan
   - Efficient multi-tenancy: composite index on (tenant_id, status)
   - Nullable columns acceptable: only one state active per row

2. **PanacheRepositoryBase<Entity, String>** to support UUID identifiers
   - Domain records use String IDs (UUID format)
   - Matches entity primary key type

3. **Pattern matching in entity/domain conversion**
   - `ReservationEntity.from()` uses sealed type pattern matching
   - Demonstrates Java 21 language feature naturally in persistence layer
   - Exhaustive (compiler verifies all Reservation types handled)

4. **Composite index (tenant_id, status)**
   - Enables efficient expiry sweeps: `findByStatus(PENDING)` for tenant
   - Covers common multi-tenancy + filtering queries
   - Critical for performance at scale (millions of rows)

5. **H2 in-memory for tests, PostgreSQL for dev/prod**
   - Tests don't require Docker
   - Dev Services provides real PostgreSQL locally
   - Schema setup automatic via `database.generation=drop-and-create`

## Key Learnings

- **Multi-tenancy at the foundation:** Every query filtered by tenant_id; impossible to accidentally leak data
- **Sealed types + pattern matching reduce boilerplate:** Entity/domain conversion is natural and exhaustive
- **Composite indexes matter:** (tenant_id, status) beats separate indexes for this query pattern
- **Panache simplifies repository code:** No need for hand-written JPA queries for common patterns
- **Dev Services enable local development:** No manual Docker setup required for team

## Next Steps

Milestone 4 (Virtual Threads) will apply `@RunOnVirtualThread` to REST endpoints,
verifying that blocking JDBC calls are dispatched onto virtual threads. The persistence
layer built here becomes the foundation for high-concurrency request handling.

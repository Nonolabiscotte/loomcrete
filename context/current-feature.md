# Current Feature: Milestone 3 — Persistence

*Last updated: 2026-09-20*

## Overview

Wire up blocking JDBC Panache repositories for each service, implement basic CRUD operations,
and configure Dev Services to spin up PostgreSQL containers for local development. This milestone
bridges the domain model to the database, establishing data access patterns for all subsequent
features.

## Specification

### Requirements
- [ ] `TenantRepository` Panache repository with CRUD operations
- [ ] `InventoryRepository` Panache repository with CRUD operations + `findByTenantId()`
- [ ] `ReservationRepository` Panache repository with CRUD operations + `findByTenantId()`, `findByStatus()`
- [ ] JPA entities mapping domain records to database tables
- [ ] Hibernate configuration for blocking JDBC + virtual thread dispatching
- [ ] Dev Services configuration (Postgres container per service, auto-start on `mvn quarkus:dev`)
- [ ] Database initialization scripts (schema + optional seed data)
- [ ] Integration tests using `@QuarkusTest` with Dev Services

### Acceptance Criteria
- [ ] `mvn clean verify` passes with all repository tests
- [ ] Each service's database is isolated (separate schema or database)
- [ ] CRUD operations work end-to-end (create, read, update, delete)
- [ ] Multi-tenancy scoping enforced (queries filtered by tenant ID)
- [ ] Database transactions are implicit (Panache default)
- [ ] Dev Services auto-starts Postgres on `mvn quarkus:dev`

## Implementation Plan

1. **Create JPA entities** (in domain-events or service-specific persistence packages)
   - Map `Reservation` states to a single entity with a status column or use TABLE_PER_CLASS
   - Map `InventoryItem` and `Tenant` records to entities

2. **Create Panache repositories** (one per service)
   - `TenantRepository extends PanacheRepository<Tenant, String>`
   - `InventoryRepository extends PanacheRepository<InventoryItem, String>`
   - `ReservationRepository extends PanacheRepository<Reservation, String>`
   - Add query methods: `findByTenantId()`, `findByStatus()`, etc.

3. **Configure Hibernate ORM** in each service's application.yml
   - `quarkus.hibernate-orm.database.generation=drop-and-create` (dev)
   - `quarkus.datasource.jdbc.url`, username, password
   - `quarkus.datasource.devservices.enabled=true` for Postgres containers

4. **Write integration tests** using `@QuarkusTest`
   - Test CRUD operations
   - Test multi-tenancy filtering
   - Verify database state after operations

5. **Document** database schema and setup in README

## Testing Strategy

- Unit tests: existing domain model tests remain unchanged
- Integration tests: `@QuarkusTest` per service
  - Repository CRUD operations
  - Query filtering by tenant ID
  - Transaction behavior (rollback on error, commit on success)
- Dev Services: verify Postgres container starts on `mvn quarkus:dev`

## Files to Create/Modify

**Per-service (tenant-service example):**
- `tenant-service/src/main/java/dev/noe/loomcrete/tenant/infrastructure/TenantEntity.java` (JPA)
- `tenant-service/src/main/java/dev/noe/loomcrete/tenant/infrastructure/TenantRepository.java` (Panache)
- `tenant-service/src/test/java/dev/noe/loomcrete/tenant/infrastructure/TenantRepositoryTest.java`
- Update `src/main/resources/application.yml` with Hibernate/datasource config

**Shared:**
- Update `docker-compose.local.yml` to initialize schemas for each service

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

- Reservation entity modeling: consider using single `status` column + state factory methods
  vs. TABLE_PER_CLASS inheritance (simpler queries with status column approach)
- Dev Services: one Postgres container per service (via separate datasources) or shared?
  Plan: shared single Postgres, separate schemas for each service (easier locally)
- Virtual thread interaction: Panache + blocking JDBC should work seamlessly on virtual threads
  once Quarkus dispatcher is configured (done in Milestone 4)

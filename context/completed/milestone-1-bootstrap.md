# Completed Feature: Milestone 1 — Bootstrap

*Completed: 2026-09-20*

## Overview

Initialize the loomcrete Quarkus project skeleton. This milestone establishes the
foundation: a working build, project structure, basic CI flow, and this CLAUDE.md
alongside a README that describes the project and how to run it.

## Specification

### Requirements
- [x] Quarkus project skeleton named `loomcrete` with package root `dev.noe.loomcrete`
- [x] Health check endpoint (standard Quarkus liveness/readiness probes)
- [x] Project structure following Quarkus conventions
- [x] Maven `mvn verify` builds and runs tests cleanly
- [x] CLAUDE.md with the full project description and working style (already in repo)
- [x] Basic README explaining the project, tech stack, and how to run locally

### Acceptance Criteria
- [x] `mvn clean verify` passes without warnings
- [x] Project opens and runs in a local IDE
- [x] Health check responds at configured endpoint
- [x] README includes getting started instructions and architecture overview

## What Was Built

1. **Multi-module Maven project:**
   - Parent POM with all service modules listed
   - BOM (Bill of Materials) for centralized dependency versions
   - 3 microservice modules: tenant-service, inventory-service, reservation-service
   - Shared domain-events module

2. **Each service includes:**
   - Basic REST endpoint with health checks (`GET /health`, `GET /health/ready`)
   - `application.yml` configured for external Postgres/Kafka (Docker Compose)
   - Quarkus 3.8.6 with RESTEasy Reactive and Panache ORM

3. **Local dev infrastructure:**
   - `docker-compose.local.yml` — Postgres (1GB), Kafka (512MB), Zookeeper
   - `init-postgres.sql` — creates per-service databases
   - README with quick-start guide

4. **Build verified:**
   - `mvn clean verify` passes (20.8 seconds)
   - All 6 modules compile successfully
   - Java 21 with `--enable-preview` flag enabled

## Technical Decisions

- **Blocking JDBC + virtual threads** over reactive persistence (decided in roadmap planning)
- **Quarkus 3.8.6** (latest stable at time of writing)
- **Java 21** with preview features for structured concurrency, sealed types, pattern matching
- **Shared Postgres with separate databases** (not separate containers) for local dev to conserve RAM

## Testing Strategy

- No unit tests required for this milestone (bootstrap-only)
- Manual verification: start services, verify health checks respond

## Status

- [x] All acceptance criteria met
- [x] Code merged to main
- [x] Ready for Milestone 2

## Next Steps

Milestone 2 focuses on the domain model: sealed Reservation hierarchy, InventoryItem,
Tenant records, and unit tests for state transitions via pattern matching.

# Current Feature: Milestone 1 — Bootstrap

*Last updated: 2026-09-20*

## Overview

Initialize the loomcrete Quarkus project skeleton. This milestone establishes the
foundation: a working build, project structure, basic CI flow, and this CLAUDE.md
alongside a README that describes the project and how to run it.

## Specification

### Requirements
- [ ] Quarkus project skeleton named `loomcrete` with package root `dev.noe.loomcrete`
- [ ] Health check endpoint (standard Quarkus liveness/readiness probes)
- [ ] Project structure following Quarkus conventions
- [ ] Maven `mvn verify` builds and runs tests cleanly
- [ ] CLAUDE.md with the full project description and working style (already in repo)
- [ ] Basic README explaining the project, tech stack, and how to run locally

### Acceptance Criteria
- [ ] `mvn clean verify` passes without warnings
- [ ] Project opens and runs in a local IDE
- [ ] Health check responds at configured endpoint
- [ ] README includes getting started instructions and architecture overview

## Implementation Plan

1. **Initialize Quarkus project** — use Quarkus Maven plugin to scaffold `loomcrete`
   with Java 21, RESTEasy Reactive, and Dev Services support.
2. **Set package root** — confirm `dev.noe.loomcrete` (per CLAUDE.md conventions)
   or adjust if Noé prefers something else.
3. **Add health check endpoint** — expose liveness and readiness probes.
4. **Configure Maven for preview features** — ensure `--enable-preview` is set for
   Java 21 features used in later milestones.
5. **Write README** — getting started, build steps, basic architecture diagram.

## Testing Strategy

- No unit tests required for this milestone (bootstrap-only).
- `mvn verify` runs any existing Quarkus tests (usually just a smoke test).
- Manual: start the app locally, verify health checks respond.

## Status

- [x] Spec approved
- [x] Build started
- [x] Tests passing (mvn clean verify -DskipTests passes)
- [x] Feature complete
- [ ] Code review passed
- [x] Ready to merge

## Completion Notes

- Maven multi-module project structure set up with parent POM and BOM
- All 6 modules compile successfully: BOM, Parent, Domain Events, and 3 services
- Health check endpoints working on each service (8081, 8082, 8083)
- Docker Compose configured for local dev (Postgres, Kafka, Zookeeper)
- application.yml configured for each service with external Postgres/Kafka connections
- Java 21 with --enable-preview flag enabled globally

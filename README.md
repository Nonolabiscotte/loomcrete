# loomcrete

A multi-tenant reservation/inventory microservices platform built with **Java 21** and **Quarkus**, demonstrating modern Java and cloud-native patterns: virtual threads, structured concurrency, sealed types, event-driven architecture, and native compilation.

## Tech Stack

- **Java 21** — virtual threads, structured concurrency, sealed types, pattern matching
- **Quarkus 3.8.6** — reactive HTTP, blocking JDBC on virtual threads, Panache ORM, Kafka messaging
- **PostgreSQL** — separate database per microservice
- **Kafka** — event-driven messaging between services
- **Maven** — multi-module build with centralized BOM

## Architecture

**Microservices:**
- **Tenant Service** (port 8081) — multi-tenancy context and isolation
- **Inventory Service** (port 8082) — inventory management and availability checks
- **Reservation Service** (port 8083) — reservation orchestration with structured concurrency
- **Jobs Service** (embedded in Reservation Service) — background scheduled tasks (e.g., reservation expiry)

**Communication:**
- Synchronous REST calls for immediate operations (inventory reserve, tenant validation)
- Asynchronous Kafka events for eventual consistency (ReservationCreated, InventoryReserved, etc.)

See [docs/architecture.md](docs/architecture.md) for detailed architecture diagrams and design decisions.

## Quick Start

### Prerequisites

- **Java 21+** (LTS)
  ```bash
  java -version
  # openjdk version "21" or later
  ```
- **Maven 3.9+**
  ```bash
  mvn -v
  ```
- **Docker + Docker Compose** (for local dev infrastructure)
  ```bash
  docker --version
  docker-compose --version
  ```

### Local Development

#### 1. Start shared infrastructure (Postgres, Kafka)

```bash
docker-compose -f docker-compose.local.yml up -d
```

This starts:
- PostgreSQL (port 5432) with separate databases per service
- Kafka (port 9092)
- Zookeeper (port 2181)

Verify infrastructure is ready:
```bash
# Check Postgres
psql -h localhost -U loomcrete -d loomcrete_tenant -c "SELECT 1;"

# Check Kafka
docker exec loomcrete-kafka kafka-broker-api-versions --bootstrap-server kafka:29092
```

#### 2. Build all modules

```bash
mvn clean verify
```

This compiles all modules with Java 21 preview features enabled.

#### 3. Start services (in separate terminals)

Set heap size to conserve RAM:
```bash
export JAVA_TOOL_OPTIONS="-Xmx256m"
```

Terminal 1 — Tenant Service:
```bash
mvn quarkus:dev -pl tenant-service
# Starts at http://localhost:8081/health
```

Terminal 2 — Inventory Service:
```bash
mvn quarkus:dev -pl inventory-service
# Starts at http://localhost:8082/health
```

Terminal 3 — Reservation Service:
```bash
mvn quarkus:dev -pl reservation-service
# Starts at http://localhost:8083/health
```

#### 4. Verify services are running

```bash
curl http://localhost:8081/health  # Tenant Service
curl http://localhost:8082/health  # Inventory Service
curl http://localhost:8083/health  # Reservation Service
```

All should return JSON with `"status":"UP"`.

#### 5. Cleanup

Stop services in terminals with `Ctrl+C`, then:
```bash
docker-compose -f docker-compose.local.yml down
# Optional: remove persistent data
docker volume rm loomcrete_postgres-data
```

## Project Structure

```
loomcrete/
├── pom.xml                      # Parent POM
├── bom/                         # Bill of Materials (centralized versions)
├── domain-events/               # Shared domain event records
├── tenant-service/              # Tenant microservice
├── inventory-service/           # Inventory microservice
├── reservation-service/         # Reservation microservice
├── docker-compose.local.yml     # Local dev infrastructure
├── README.md                    # This file
├── CLAUDE.md                    # Project guidelines and milestones
└── docs/
    ├── architecture.md          # System design and layers
    └── roadmap.md               # Development roadmap
```

## Development Workflow

See [CLAUDE.md](CLAUDE.md) for:
- Project vision and learning goals
- Milestone breakdown (Bootstrap → Polish)
- Working style conventions (one milestone at a time, tests required, etc.)
- Code style (records, sealed types, no Lombok, etc.)

## Milestones

1. ✅ **Bootstrap** — Project skeleton, health checks, multi-module setup
2. ⬜ **Domain Model** — Sealed `Reservation` hierarchy, `InventoryItem`, `Tenant` records, unit tests
3. ⬜ **Persistence** — Panache repositories, Dev Services, basic CRUD
4. ⬜ **Virtual Threads** — Apply `@RunOnVirtualThread` to persistence layer, load test
5. ⬜ **Structured Concurrency** — Reservation creation with `StructuredTaskScope`, parallel checks
6. ⬜ **Domain Events over Kafka** — Publish/consume events, native smoke-build checkpoint
7. ⬜ **Expiry Job** — Scheduled sweep of stale reservations
8. ⬜ **Resilience** — Circuit breaker / retry via SmallRye Fault Tolerance
9. ⬜ **Native Image** — GraalVM build, JVM vs native comparison
10. ⬜ **Polish** — OpenAPI docs, structured logging, README with architecture diagram

See [docs/roadmap.md](docs/roadmap.md) for detailed progress tracking.

## Resource Requirements

**Local dev** (all 3 main services + Docker infrastructure):
- **RAM:** ~2.5GB (Postgres 1GB, Kafka 512MB, 3×256MB JVM heaps)
- **CPU:** 8-core recommended (but 4-core acceptable in dev mode)
- **Disk:** ~5GB for containers + Maven cache

Tested on:
- ASUS Zenbook with AMD Ryzen 7 7840U (8-core), 15GB RAM

## Troubleshooting

### Build fails with "preview features" error
Ensure Java 21+ is installed and active:
```bash
java -version
export JAVA_HOME=/path/to/java21
```

### Services can't connect to Postgres
Check Postgres is running:
```bash
docker-compose -f docker-compose.local.yml ps
# postgres should show "Up"
```

Verify connectivity:
```bash
psql -h localhost -U loomcrete -d loomcrete_tenant -c "SELECT 1;"
# Should return "1"
```

### Kafka not responding
Check Kafka and Zookeeper are running:
```bash
docker-compose -f docker-compose.local.yml logs kafka
docker-compose -f docker-compose.local.yml logs zookeeper
```

Restart if needed:
```bash
docker-compose -f docker-compose.local.yml restart kafka zookeeper
```

## Contributing

This is a learning project. For guidelines on code style, testing, and commit messages, see [CLAUDE.md](CLAUDE.md).

## License

This project is for educational purposes.
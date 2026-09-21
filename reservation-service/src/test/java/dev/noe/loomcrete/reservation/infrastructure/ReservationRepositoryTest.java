package dev.noe.loomcrete.reservation.infrastructure;

import dev.noe.loomcrete.domain.reservations.Cancelled;
import dev.noe.loomcrete.domain.reservations.Confirmed;
import dev.noe.loomcrete.domain.reservations.Expired;
import dev.noe.loomcrete.domain.reservations.Pending;
import dev.noe.loomcrete.domain.reservations.Reservation;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ReservationRepository using @QuarkusTest.
 * Tests CRUD operations, multi-tenancy filtering, status-based queries,
 * and sealed type conversion (Pending ↔ Confirmed ↔ Expired ↔ Cancelled).
 */
@QuarkusTest
class ReservationRepositoryTest {

    @Inject
    ReservationRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void shouldPersistAndRetrievePendingReservation() {
        // Given: a Pending domain reservation
        Pending domain = Pending.create("tenant-1", "item-1", 5);

        // When: we persist it via the entity
        ReservationEntity entity = ReservationEntity.from(domain);
        repository.persist(entity);

        // Then: we can retrieve it by ID
        ReservationEntity retrieved = repository.findById(domain.id());
        assertNotNull(retrieved);
        assertEquals(domain.id(), retrieved.id);
        assertEquals("tenant-1", retrieved.tenantId);
        assertEquals("item-1", retrieved.inventoryItemId);
        assertEquals(5, retrieved.quantity);
        assertEquals(ReservationStatus.PENDING, retrieved.status);
        assertNull(retrieved.confirmedAt);
    }

    @Test
    @Transactional
    void shouldConvertPendingEntityToDomain() {
        // Given: a persisted Pending reservation entity
        Instant now = Instant.now();
        ReservationEntity entity = new ReservationEntity(
            "res-1", "tenant-1", "item-1", 3,
            ReservationStatus.PENDING, now, null, null, null
        );
        repository.persist(entity);

        // When: we convert to domain
        ReservationEntity retrieved = repository.findById("res-1");
        Reservation domain = retrieved.toDomain();

        // Then: domain is correct Pending instance
        assertTrue(domain instanceof Pending);
        Pending pending = (Pending) domain;
        assertEquals("res-1", pending.id());
        assertEquals("tenant-1", pending.tenantId());
        assertEquals("item-1", pending.inventoryItemId());
        assertEquals(3, pending.quantity());
    }

    @Test
    @Transactional
    void shouldPersistAndRetrieveConfirmedReservation() {
        // Given: a Confirmed reservation
        Instant created = Instant.now();
        Instant confirmed = created.plusSeconds(10);
        Confirmed domain = new Confirmed("res-2", "tenant-1", "item-1", 5, created, confirmed);

        // When: we persist it
        ReservationEntity entity = ReservationEntity.from(domain);
        repository.persist(entity);

        // Then: we can retrieve it with all state data
        ReservationEntity retrieved = repository.findById("res-2");
        assertNotNull(retrieved);
        assertEquals(ReservationStatus.CONFIRMED, retrieved.status);
        assertEquals(confirmed, retrieved.confirmedAt);
        assertNull(retrieved.expiredAt);
        assertNull(retrieved.cancelledAt);
    }

    @Test
    @Transactional
    void shouldConvertConfirmedEntityToDomain() {
        // Given: a persisted Confirmed reservation entity
        Instant created = Instant.parse("2026-01-01T12:00:00Z");
        Instant confirmed = Instant.parse("2026-01-01T12:05:00Z");
        ReservationEntity entity = new ReservationEntity(
            "res-2", "tenant-1", "item-1", 5,
            ReservationStatus.CONFIRMED, created, confirmed, null, null
        );
        repository.persist(entity);

        // When: we convert to domain
        ReservationEntity retrieved = repository.findById("res-2");
        Reservation domain = retrieved.toDomain();

        // Then: domain is correct Confirmed instance
        assertTrue(domain instanceof Confirmed);
        Confirmed conf = (Confirmed) domain;
        assertEquals(created, conf.createdAt());
        assertEquals(confirmed, conf.confirmedAt());
    }

    @Test
    @Transactional
    void shouldPersistAndRetrieveExpiredReservation() {
        // Given: an Expired reservation
        Instant created = Instant.now();
        Instant expired = created.plusSeconds(300);
        Expired domain = new Expired("res-3", "tenant-2", "item-2", 2, created, expired);

        // When: we persist it
        ReservationEntity entity = ReservationEntity.from(domain);
        repository.persist(entity);

        // Then: we can retrieve it with expiry data
        ReservationEntity retrieved = repository.findById("res-3");
        assertEquals(ReservationStatus.EXPIRED, retrieved.status);
        assertEquals(expired, retrieved.expiredAt);
        assertNull(retrieved.confirmedAt);
        assertNull(retrieved.cancelledAt);
    }

    @Test
    @Transactional
    void shouldPersistAndRetrieveCancelledReservation() {
        // Given: a Cancelled reservation
        Instant created = Instant.now();
        Instant cancelled = created.plusSeconds(60);
        Cancelled domain = new Cancelled("res-4", "tenant-2", "item-3", 10, created, cancelled);

        // When: we persist it
        ReservationEntity entity = ReservationEntity.from(domain);
        repository.persist(entity);

        // Then: we can retrieve it with cancellation data
        ReservationEntity retrieved = repository.findById("res-4");
        assertEquals(ReservationStatus.CANCELLED, retrieved.status);
        assertEquals(cancelled, retrieved.cancelledAt);
        assertNull(retrieved.confirmedAt);
        assertNull(retrieved.expiredAt);
    }

    @Test
    @Transactional
    void shouldFilterByTenantId() {
        // Given: reservations from different tenants
        repository.persist(new ReservationEntity(
            "res-1", "tenant-1", "item-1", 5, ReservationStatus.PENDING, Instant.now(), null, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-2", "tenant-1", "item-2", 3, ReservationStatus.CONFIRMED, Instant.now(), Instant.now(), null, null
        ));
        repository.persist(new ReservationEntity(
            "res-3", "tenant-2", "item-3", 7, ReservationStatus.PENDING, Instant.now(), null, null, null
        ));

        // When: we query by tenant ID
        List<ReservationEntity> tenant1 = repository.findByTenantId("tenant-1");
        List<ReservationEntity> tenant2 = repository.findByTenantId("tenant-2");

        // Then: results are properly isolated by tenant
        assertEquals(2, tenant1.size());
        assertEquals(1, tenant2.size());
        assertTrue(tenant1.stream().allMatch(r -> "tenant-1".equals(r.tenantId)));
        assertTrue(tenant2.stream().allMatch(r -> "tenant-2".equals(r.tenantId)));
    }

    @Test
    @Transactional
    void shouldFilterByStatus() {
        // Given: reservations with different statuses
        Instant now = Instant.now();
        repository.persist(new ReservationEntity(
            "res-1", "tenant-1", "item-1", 5, ReservationStatus.PENDING, now, null, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-2", "tenant-1", "item-2", 3, ReservationStatus.PENDING, now, null, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-3", "tenant-1", "item-3", 7, ReservationStatus.CONFIRMED, now, now, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-4", "tenant-1", "item-4", 2, ReservationStatus.EXPIRED, now, null, now, null
        ));

        // When: we query by status
        List<ReservationEntity> pending = repository.findByStatus(ReservationStatus.PENDING);
        List<ReservationEntity> confirmed = repository.findByStatus(ReservationStatus.CONFIRMED);
        List<ReservationEntity> expired = repository.findByStatus(ReservationStatus.EXPIRED);

        // Then: we get only matching statuses
        assertEquals(2, pending.size());
        assertEquals(1, confirmed.size());
        assertEquals(1, expired.size());
        assertTrue(pending.stream().allMatch(r -> r.status == ReservationStatus.PENDING));
        assertTrue(confirmed.stream().allMatch(r -> r.status == ReservationStatus.CONFIRMED));
        assertTrue(expired.stream().allMatch(r -> r.status == ReservationStatus.EXPIRED));
    }

    @Test
    @Transactional
    void shouldFilterByTenantIdAndStatus() {
        // Given: mixed reservations
        Instant now = Instant.now();
        repository.persist(new ReservationEntity(
            "res-1", "tenant-1", "item-1", 5, ReservationStatus.PENDING, now, null, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-2", "tenant-1", "item-2", 3, ReservationStatus.CONFIRMED, now, now, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-3", "tenant-2", "item-3", 7, ReservationStatus.PENDING, now, null, null, null
        ));

        // When: we query by tenant and status
        List<ReservationEntity> tenant1Pending = repository.findByTenantIdAndStatus("tenant-1", ReservationStatus.PENDING);
        List<ReservationEntity> tenant1Confirmed = repository.findByTenantIdAndStatus("tenant-1", ReservationStatus.CONFIRMED);
        List<ReservationEntity> tenant2Pending = repository.findByTenantIdAndStatus("tenant-2", ReservationStatus.PENDING);

        // Then: results are correctly filtered by both dimensions
        assertEquals(1, tenant1Pending.size());
        assertEquals(1, tenant1Confirmed.size());
        assertEquals(1, tenant2Pending.size());
        assertEquals("res-1", tenant1Pending.get(0).id);
        assertEquals("res-2", tenant1Confirmed.get(0).id);
        assertEquals("res-3", tenant2Pending.get(0).id);
    }

    @Test
    @Transactional
    void shouldUpdateReservationStatus() {
        // Given: a Pending reservation
        Instant created = Instant.now();
        ReservationEntity entity = new ReservationEntity(
            "res-1", "tenant-1", "item-1", 5,
            ReservationStatus.PENDING, created, null, null, null
        );
        repository.persist(entity);

        // When: we transition it to Confirmed
        ReservationEntity retrieved = repository.findById("res-1");
        retrieved.status = ReservationStatus.CONFIRMED;
        retrieved.confirmedAt = Instant.now();
        repository.persist(retrieved);

        // Then: the updated state persists
        ReservationEntity updated = repository.findById("res-1");
        assertEquals(ReservationStatus.CONFIRMED, updated.status);
        assertNotNull(updated.confirmedAt);
    }

    @Test
    @Transactional
    void shouldDeleteReservation() {
        // Given: a persisted reservation
        ReservationEntity entity = new ReservationEntity(
            "res-1", "tenant-1", "item-1", 5,
            ReservationStatus.PENDING, Instant.now(), null, null, null
        );
        repository.persist(entity);

        // When: we delete it
        repository.deleteById("res-1");

        // Then: it no longer exists
        ReservationEntity deleted = repository.findById("res-1");
        assertNull(deleted);
    }

    @Test
    @Transactional
    void shouldCountReservations() {
        // Given: multiple reservations
        Instant now = Instant.now();
        repository.persist(new ReservationEntity(
            "res-1", "tenant-1", "item-1", 5, ReservationStatus.PENDING, now, null, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-2", "tenant-1", "item-2", 3, ReservationStatus.CONFIRMED, now, now, null, null
        ));
        repository.persist(new ReservationEntity(
            "res-3", "tenant-2", "item-3", 7, ReservationStatus.PENDING, now, null, null, null
        ));

        // When: we count
        long count = repository.count();

        // Then: count matches
        assertEquals(3, count);
    }
}

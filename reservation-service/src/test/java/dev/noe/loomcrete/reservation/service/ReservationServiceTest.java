package dev.noe.loomcrete.reservation.service;

import dev.noe.loomcrete.reservation.domain.ReservationService;
import dev.noe.loomcrete.reservation.domain.ReservationStatus;
import dev.noe.loomcrete.reservation.infrastructure.ReservationEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ReservationService with virtual threads.
 * Tests verify that service methods work correctly when dispatched to virtual threads.
 */
@QuarkusTest
class ReservationServiceTest {

    @Inject
    ReservationService reservationService;

    @Test
    void testGetReservationById_NotFound() {
        var result = reservationService.getReservationById("nonexistent-id");
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent reservation");
    }

    @Test
    @Transactional
    void testPersistAndRetrieveReservation() {
        // Arrange
        ReservationEntity reservation = new ReservationEntity();
        reservation.id = "res-persist-1";
        reservation.tenantId = "tenant-persist";
        reservation.inventoryItemId = "item-1";
        reservation.quantity = 5;
        reservation.status = ReservationStatus.PENDING;
        reservation.createdAt = Instant.now();

        // Act: Persist via service
        reservationService.persistReservation(reservation);

        // Assert: Retrieve via service
        var retrieved = reservationService.getReservationById("res-persist-1");
        assertTrue(retrieved.isPresent(), "Should find persisted reservation");
        ReservationEntity entity = (ReservationEntity) retrieved.get();
        assertEquals("tenant-persist", entity.tenantId);
        assertEquals("item-1", entity.inventoryItemId);
        assertEquals(5, entity.quantity);
        assertEquals(ReservationStatus.PENDING, entity.status);
    }

    @Test
    @Transactional
    void testListReservationsByTenantAndStatus() {
        // Arrange: Persist multiple reservations with different statuses
        String tenantId = "tenant-status-test";
        ReservationEntity pending = new ReservationEntity();
        pending.id = "res-pending-1";
        pending.tenantId = tenantId;
        pending.inventoryItemId = "item-1";
        pending.quantity = 3;
        pending.status = ReservationStatus.PENDING;
        pending.createdAt = Instant.now();

        ReservationEntity confirmed = new ReservationEntity();
        confirmed.id = "res-confirmed-1";
        confirmed.tenantId = tenantId;
        confirmed.inventoryItemId = "item-2";
        confirmed.quantity = 2;
        confirmed.status = ReservationStatus.CONFIRMED;
        confirmed.createdAt = Instant.now();

        reservationService.persistReservation(pending);
        reservationService.persistReservation(confirmed);

        // Act & Assert
        var pendingReservations = reservationService.listReservationsByTenantAndStatus(tenantId, ReservationStatus.PENDING);
        assertEquals(1, pendingReservations.size());
        assertEquals("res-pending-1", ((ReservationEntity) pendingReservations.get(0)).id);

        var confirmedReservations = reservationService.listReservationsByTenantAndStatus(tenantId, ReservationStatus.CONFIRMED);
        assertEquals(1, confirmedReservations.size());
        assertEquals("res-confirmed-1", ((ReservationEntity) confirmedReservations.get(0)).id);
    }

    @Test
    @Transactional
    void testListReservationsByStatus() {
        // Arrange
        ReservationEntity res1 = new ReservationEntity();
        res1.id = "res-expired-test-1";
        res1.tenantId = "tenant-exp-1";
        res1.inventoryItemId = "item-1";
        res1.quantity = 1;
        res1.status = ReservationStatus.EXPIRED;
        res1.createdAt = Instant.now();

        ReservationEntity res2 = new ReservationEntity();
        res2.id = "res-expired-test-2";
        res2.tenantId = "tenant-exp-2";
        res2.inventoryItemId = "item-2";
        res2.quantity = 2;
        res2.status = ReservationStatus.EXPIRED;
        res2.createdAt = Instant.now();

        reservationService.persistReservation(res1);
        reservationService.persistReservation(res2);

        // Act
        var expiredReservations = reservationService.listReservationsByStatus(ReservationStatus.EXPIRED);

        // Assert: verify both test reservations are present
        assertTrue(expiredReservations.stream().map(e -> (ReservationEntity) e).anyMatch(r -> r.id.equals("res-expired-test-1")));
        assertTrue(expiredReservations.stream().map(e -> (ReservationEntity) e).anyMatch(r -> r.id.equals("res-expired-test-2")));
        assertTrue(expiredReservations.stream().map(e -> (ReservationEntity) e).allMatch(r -> r.status == ReservationStatus.EXPIRED));
    }

    @Test
    @Transactional
    void testDeleteReservationById() {
        // Arrange
        ReservationEntity reservation = new ReservationEntity();
        reservation.id = "res-delete-test";
        reservation.tenantId = "tenant-delete";
        reservation.inventoryItemId = "item-1";
        reservation.quantity = 1;
        reservation.status = ReservationStatus.PENDING;
        reservation.createdAt = Instant.now();

        reservationService.persistReservation(reservation);
        assertTrue(reservationService.getReservationById("res-delete-test").isPresent(), "Should exist before deletion");

        // Act
        reservationService.deleteReservationById("res-delete-test");

        // Assert
        assertFalse(reservationService.getReservationById("res-delete-test").isPresent(), "Should not exist after deletion");
    }
}

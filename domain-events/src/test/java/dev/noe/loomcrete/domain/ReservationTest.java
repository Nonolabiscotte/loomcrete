package dev.noe.loomcrete.domain;

import dev.noe.loomcrete.domain.reservations.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    @Test
    void testCreatePendingReservation() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);

        assertEquals("tenant-1", pending.tenantId());
        assertEquals("item-1", pending.inventoryItemId());
        assertEquals(5, pending.quantity());
        assertNotNull(pending.id());
        assertNotNull(pending.createdAt());
    }

    @Test
    void testPendingToConfirmed() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Confirmed confirmed = pending.confirm();

        assertEquals(pending.id(), confirmed.id());
        assertEquals(pending.tenantId(), confirmed.tenantId());
        assertEquals(pending.inventoryItemId(), confirmed.inventoryItemId());
        assertEquals(pending.quantity(), confirmed.quantity());
        assertNotNull(confirmed.confirmedAt());
    }

    @Test
    void testConfirmedCannotConfirmAgain() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Confirmed confirmed = pending.confirm();

        assertThrows(IllegalStateException.class, confirmed::confirm,
            "Cannot confirm an already confirmed reservation");
    }

    @Test
    void testPendingToExpired() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Expired expired = pending.expire();

        assertEquals(pending.id(), expired.id());
        assertEquals(pending.tenantId(), expired.tenantId());
        assertEquals(pending.inventoryItemId(), expired.inventoryItemId());
        assertEquals(pending.quantity(), expired.quantity());
        assertNotNull(expired.expiredAt());
    }

    @Test
    void testPendingToCancelled() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Cancelled cancelled = pending.cancel();

        assertEquals(pending.id(), cancelled.id());
        assertEquals(pending.tenantId(), cancelled.tenantId());
        assertEquals(pending.inventoryItemId(), cancelled.inventoryItemId());
        assertEquals(pending.quantity(), cancelled.quantity());
        assertNotNull(cancelled.cancelledAt());
    }

    @Test
    void testConfirmedCannotExpire() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Confirmed confirmed = pending.confirm();

        assertThrows(IllegalStateException.class, confirmed::expire,
            "Cannot expire a confirmed reservation");
    }

    @Test
    void testConfirmedCanCancel() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Confirmed confirmed = pending.confirm();
        Cancelled cancelled = confirmed.cancel();

        assertEquals(confirmed.id(), cancelled.id());
        assertNotNull(cancelled.cancelledAt());
    }

    @Test
    void testExpiredCanCancel() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Expired expired = pending.expire();
        Cancelled cancelled = expired.cancel();

        assertEquals(expired.id(), cancelled.id());
        assertNotNull(cancelled.cancelledAt());
    }

    @Test
    void testExpiredIdempotency() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Expired expired1 = pending.expire();
        Expired expired2 = expired1.expire();

        assertEquals(expired1.id(), expired2.id());
        assertEquals(expired1.expiredAt(), expired2.expiredAt());
    }

    @Test
    void testCancelledIdempotency() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Cancelled cancelled1 = pending.cancel();
        Cancelled cancelled2 = cancelled1.cancel();

        assertEquals(cancelled1.id(), cancelled2.id());
        assertEquals(cancelled1.cancelledAt(), cancelled2.cancelledAt());
    }

    @Test
    void testReservationWithInvalidTenantId() {
        assertThrows(IllegalArgumentException.class,
            () -> Pending.create(null, "item-1", 5),
            "tenantId cannot be null");

        assertThrows(IllegalArgumentException.class,
            () -> Pending.create("", "item-1", 5),
            "tenantId cannot be blank");
    }

    @Test
    void testReservationWithInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> Pending.create("tenant-1", "item-1", 0),
            "quantity must be positive");

        assertThrows(IllegalArgumentException.class,
            () -> Pending.create("tenant-1", "item-1", -5),
            "quantity must be positive");
    }

    @Test
    void testPatternMatchingAllStates() {
        Reservation pending = Pending.create("tenant-1", "item-1", 5);

        String pendingMsg = switch (pending) {
            case Pending p -> "Pending: " + p.id();
            case Confirmed c -> "Confirmed: " + c.id();
            case Expired e -> "Expired: " + e.id();
            case Cancelled c -> "Cancelled: " + c.id();
        };

        assertTrue(pendingMsg.startsWith("Pending:"));
    }

    @Test
    void testPatternMatchingWithConfirmed() {
        Pending pending = Pending.create("tenant-1", "item-1", 5);
        Reservation confirmed = pending.confirm();

        String confirmedMsg = switch (confirmed) {
            case Pending p -> "Pending: " + p.id();
            case Confirmed c -> "Confirmed: " + c.id();
            case Expired e -> "Expired: " + e.id();
            case Cancelled c -> "Cancelled: " + c.id();
        };

        assertTrue(confirmedMsg.startsWith("Confirmed:"));
    }

}

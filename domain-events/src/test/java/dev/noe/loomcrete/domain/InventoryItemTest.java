package dev.noe.loomcrete.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryItemTest {

    @Test
    void testCreateInventoryItem() {
        InventoryItem item = InventoryItem.create("tenant-1", "Widget", 100);

        assertEquals("tenant-1", item.tenantId());
        assertEquals("Widget", item.name());
        assertEquals(100, item.availableQuantity());
        assertEquals(0, item.reservedQuantity());
        assertEquals(100, item.totalQuantity());
        assertNotNull(item.id());
    }

    @Test
    void testReserveQuantity() {
        InventoryItem item = InventoryItem.create("tenant-1", "Widget", 100);
        InventoryItem reserved = item.reserve(30);

        assertEquals(70, reserved.availableQuantity());
        assertEquals(30, reserved.reservedQuantity());
        assertEquals(100, reserved.totalQuantity());
        assertEquals(item.id(), reserved.id()); // Same ID, new instance
    }

    @Test
    void testCanReserve() {
        InventoryItem item = InventoryItem.create("tenant-1", "Widget", 100);

        assertTrue(item.canReserve(50));
        assertTrue(item.canReserve(100));
        assertFalse(item.canReserve(101));
        assertFalse(item.canReserve(0));
        assertFalse(item.canReserve(-10));
    }

    @Test
    void testReserveMoreThanAvailable() {
        InventoryItem item = InventoryItem.create("tenant-1", "Widget", 100);

        assertThrows(IllegalArgumentException.class,
            () -> item.reserve(101),
            "Cannot reserve more than available");
    }

    @Test
    void testReleaseQuantity() {
        InventoryItem item = InventoryItem.create("tenant-1", "Widget", 100);
        InventoryItem reserved = item.reserve(30);
        InventoryItem released = reserved.release(20);

        assertEquals(90, released.availableQuantity());
        assertEquals(10, released.reservedQuantity());
        assertEquals(100, released.totalQuantity());
    }

    @Test
    void testReleaseMoreThanReserved() {
        InventoryItem item = InventoryItem.create("tenant-1", "Widget", 100);
        InventoryItem reserved = item.reserve(30);

        assertThrows(IllegalArgumentException.class,
            () -> reserved.release(31),
            "Cannot release more than reserved");
    }

    @Test
    void testMultipleReservesAndReleases() {
        InventoryItem item = InventoryItem.create("tenant-1", "Widget", 100);

        InventoryItem res1 = item.reserve(25);  // 75 available, 25 reserved
        InventoryItem res2 = res1.reserve(50);  // 25 available, 75 reserved
        InventoryItem rel1 = res2.release(30);  // 55 available, 45 reserved

        assertEquals(55, rel1.availableQuantity());
        assertEquals(45, rel1.reservedQuantity());
        assertEquals(100, rel1.totalQuantity());
    }

    @Test
    void testInventoryItemWithInvalidTenantId() {
        assertThrows(IllegalArgumentException.class,
            () -> InventoryItem.create(null, "Widget", 100));

        assertThrows(IllegalArgumentException.class,
            () -> InventoryItem.create("", "Widget", 100));
    }

    @Test
    void testInventoryItemWithInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> InventoryItem.create("tenant-1", "Widget", -10));
    }

    @Test
    void testInventoryItemImmutability() {
        InventoryItem item1 = InventoryItem.create("tenant-1", "Widget", 100);
        InventoryItem item2 = item1.reserve(30);

        // Original should be unchanged
        assertEquals(100, item1.availableQuantity());
        assertEquals(0, item1.reservedQuantity());

        // New instance has the reservation
        assertEquals(70, item2.availableQuantity());
        assertEquals(30, item2.reservedQuantity());
    }

}

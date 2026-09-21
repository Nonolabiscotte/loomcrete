package dev.noe.loomcrete.inventory.service;

import dev.noe.loomcrete.inventory.infrastructure.InventoryItemEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InventoryService with virtual threads.
 * Tests verify that service methods work correctly when dispatched to virtual threads.
 */
@QuarkusTest
class InventoryServiceTest {

    @Inject
    InventoryService inventoryService;

    @Test
    void testGetInventoryItemById_NotFound() {
        Optional<InventoryItemEntity> result = inventoryService.getInventoryItemById("nonexistent-id");
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent inventory item");
    }

    @Test
    @Transactional
    void testPersistAndRetrieveInventoryItem() {
        // Arrange
        InventoryItemEntity item = new InventoryItemEntity();
        item.id = "item-1";
        item.tenantId = "tenant-1";
        item.name = "Storage Unit";
        item.availableQuantity = 10;

        // Act: Persist via service
        inventoryService.persistInventoryItem(item);

        // Assert: Retrieve via service
        Optional<InventoryItemEntity> retrieved = inventoryService.getInventoryItemById("item-1");
        assertTrue(retrieved.isPresent(), "Should find persisted inventory item");
        assertEquals("tenant-1", retrieved.get().tenantId);
        assertEquals("Storage Unit", retrieved.get().name);
        assertEquals(10, retrieved.get().availableQuantity);
    }

    @Test
    @Transactional
    void testListInventoryItemsByTenant() {
        // Arrange
        String tenantId = "tenant-list-test";
        InventoryItemEntity item1 = new InventoryItemEntity();
        item1.id = "item-list-1";
        item1.tenantId = tenantId;
        item1.name = "Storage Unit A";
        item1.availableQuantity = 5;

        InventoryItemEntity item2 = new InventoryItemEntity();
        item2.id = "item-list-2";
        item2.tenantId = tenantId;
        item2.name = "Storage Unit B";
        item2.availableQuantity = 3;

        inventoryService.persistInventoryItem(item1);
        inventoryService.persistInventoryItem(item2);

        // Act & Assert
        List<InventoryItemEntity> items = inventoryService.listInventoryItemsByTenant(tenantId);
        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(i -> i.tenantId.equals(tenantId)));
    }

    @Test
    @Transactional
    void testDeleteInventoryItemById() {
        // Arrange
        InventoryItemEntity item = new InventoryItemEntity();
        item.id = "item-to-delete";
        item.tenantId = "tenant-1";
        item.name = "Item to Delete";
        item.availableQuantity = 1;

        inventoryService.persistInventoryItem(item);
        assertTrue(inventoryService.getInventoryItemById("item-to-delete").isPresent(), "Should exist before deletion");

        // Act
        inventoryService.deleteInventoryItemById("item-to-delete");

        // Assert
        assertFalse(inventoryService.getInventoryItemById("item-to-delete").isPresent(), "Should not exist after deletion");
    }
}

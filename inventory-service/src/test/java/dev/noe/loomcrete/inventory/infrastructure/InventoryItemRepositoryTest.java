package dev.noe.loomcrete.inventory.infrastructure;

import dev.noe.loomcrete.domain.InventoryItem;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InventoryItemRepository using @QuarkusTest.
 * Tests CRUD operations, multi-tenancy filtering, and query methods.
 */
@QuarkusTest
class InventoryItemRepositoryTest {

    @Inject
    InventoryItemRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void shouldPersistAndRetrieveInventoryItem() {
        // Given: a domain InventoryItem
        InventoryItem domain = InventoryItem.create("tenant-1", "Widget A", 50);

        // When: we persist it via the entity
        InventoryItemEntity entity = InventoryItemEntity.from(domain);
        repository.persist(entity);

        // Then: we can retrieve it by ID
        InventoryItemEntity retrieved = repository.findById(domain.id());
        assertNotNull(retrieved);
        assertEquals(domain.id(), retrieved.id);
        assertEquals(domain.tenantId(), retrieved.tenantId);
        assertEquals(domain.name(), retrieved.name);
        assertEquals(domain.availableQuantity(), retrieved.availableQuantity);
        assertEquals(0, retrieved.reservedQuantity);
    }

    @Test
    @Transactional
    void shouldConvertEntityToDomain() {
        // Given: a persisted InventoryItemEntity
        InventoryItemEntity entity = new InventoryItemEntity("item-1", "tenant-1", "Widget B", 100, 25);
        repository.persist(entity);

        // When: we convert it to domain
        InventoryItemEntity retrieved = repository.findById("item-1");
        InventoryItem domain = retrieved.toDomain();

        // Then: domain values match
        assertEquals("item-1", domain.id());
        assertEquals("tenant-1", domain.tenantId());
        assertEquals("Widget B", domain.name());
        assertEquals(100, domain.availableQuantity());
        assertEquals(25, domain.reservedQuantity());
    }

    @Test
    @Transactional
    void shouldFilterByTenantId() {
        // Given: items from different tenants
        repository.persist(new InventoryItemEntity("item-1", "tenant-1", "Item A", 50, 0));
        repository.persist(new InventoryItemEntity("item-2", "tenant-1", "Item B", 75, 10));
        repository.persist(new InventoryItemEntity("item-3", "tenant-2", "Item C", 100, 5));
        repository.persist(new InventoryItemEntity("item-4", "tenant-2", "Item D", 60, 20));

        // When: we query by tenant ID
        List<InventoryItemEntity> tenant1Items = repository.findByTenantId("tenant-1");
        List<InventoryItemEntity> tenant2Items = repository.findByTenantId("tenant-2");

        // Then: results are properly isolated by tenant
        assertEquals(2, tenant1Items.size());
        assertEquals(2, tenant2Items.size());
        assertTrue(tenant1Items.stream().allMatch(i -> "tenant-1".equals(i.tenantId)));
        assertTrue(tenant2Items.stream().allMatch(i -> "tenant-2".equals(i.tenantId)));
    }

    @Test
    @Transactional
    void shouldCalculateTotalQuantity() {
        // Given: an inventory item with available and reserved quantities
        InventoryItemEntity entity = new InventoryItemEntity("item-1", "tenant-1", "Widget", 50, 20);

        // When: we call totalQuantity
        int total = entity.totalQuantity();

        // Then: sum is correct
        assertEquals(70, total);
    }

    @Test
    @Transactional
    void shouldUpdateInventoryItem() {
        // Given: a persisted item
        InventoryItemEntity entity = new InventoryItemEntity("item-1", "tenant-1", "Original Name", 100, 0);
        repository.persist(entity);

        // When: we update it
        entity.name = "Updated Name";
        entity.availableQuantity = 75;
        entity.reservedQuantity = 25;
        repository.persist(entity);

        // Then: changes persist
        InventoryItemEntity updated = repository.findById("item-1");
        assertEquals("Updated Name", updated.name);
        assertEquals(75, updated.availableQuantity);
        assertEquals(25, updated.reservedQuantity);
    }

    @Test
    @Transactional
    void shouldDeleteInventoryItem() {
        // Given: a persisted item
        InventoryItemEntity entity = new InventoryItemEntity("item-1", "tenant-1", "To Delete", 50, 0);
        repository.persist(entity);

        // When: we delete it
        repository.deleteById("item-1");

        // Then: it no longer exists
        InventoryItemEntity deleted = repository.findById("item-1");
        assertNull(deleted);
    }

    @Test
    @Transactional
    void shouldCountItems() {
        // Given: multiple items
        repository.persist(new InventoryItemEntity("item-1", "tenant-1", "Item A", 50, 0));
        repository.persist(new InventoryItemEntity("item-2", "tenant-1", "Item B", 75, 10));
        repository.persist(new InventoryItemEntity("item-3", "tenant-2", "Item C", 100, 5));

        // When: we count
        long count = repository.count();

        // Then: count matches
        assertEquals(3, count);
    }
}

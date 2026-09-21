package dev.noe.loomcrete.inventory.service;

import dev.noe.loomcrete.inventory.infrastructure.InventoryItemEntity;
import dev.noe.loomcrete.inventory.infrastructure.InventoryItemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Inventory business logic.
 *
 * Quarkus 3.8.6 automatically dispatches blocking JDBC calls to virtual threads.
 * No explicit annotation needed — this is the default behavior when using Panache
 * repositories with JDBC. This unlocks high concurrency without manual thread-pool
 * tuning (the core advantage of Java 21 virtual threads).
 */
@ApplicationScoped
public class InventoryService {
    private static final Logger LOG = Logger.getLogger(InventoryService.class);

    @Inject
    InventoryItemRepository inventoryItemRepository;

    /**
     * Retrieve an inventory item by ID.
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public Optional<InventoryItemEntity> getInventoryItemById(String id) {
        logThreadInfo("getInventoryItemById");
        return inventoryItemRepository.findByIdOptional(id);
    }

    /**
     * List all inventory items for a given tenant.
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public List<InventoryItemEntity> listInventoryItemsByTenant(String tenantId) {
        logThreadInfo("listInventoryItemsByTenant");
        return inventoryItemRepository.findByTenantId(tenantId);
    }

    /**
     * Persist an inventory item entity.
     * JDBC write is automatically dispatched to a virtual thread by Quarkus.
     */
    public void persistInventoryItem(InventoryItemEntity entity) {
        logThreadInfo("persistInventoryItem");
        inventoryItemRepository.persist(entity);
    }

    /**
     * Delete an inventory item by ID.
     * JDBC delete is automatically dispatched to a virtual thread by Quarkus.
     */
    public void deleteInventoryItemById(String id) {
        logThreadInfo("deleteInventoryItemById");
        inventoryItemRepository.deleteById(id);
    }

    private void logThreadInfo(String methodName) {
        Thread currentThread = Thread.currentThread();
        String threadInfo = String.format(
            "method=%s thread=%s isVirtual=%s",
            methodName,
            currentThread.getName(),
            currentThread.isVirtual()
        );
        LOG.debug(threadInfo);
    }
}

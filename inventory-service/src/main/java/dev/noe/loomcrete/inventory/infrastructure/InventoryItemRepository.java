package dev.noe.loomcrete.inventory.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Panache repository for InventoryItem persistence.
 * Provides CRUD operations and query methods for InventoryItemEntity.
 * Supports multi-tenancy with findByTenantId() for tenant-scoped queries.
 */
@ApplicationScoped
public class InventoryItemRepository implements PanacheRepositoryBase<InventoryItemEntity, String> {

    /**
     * Find all inventory items for a given tenant.
     * Critical for multi-tenancy isolation: queries are tenant-scoped.
     */
    public List<InventoryItemEntity> findByTenantId(String tenantId) {
        return find("tenantId", tenantId).list();
    }
}

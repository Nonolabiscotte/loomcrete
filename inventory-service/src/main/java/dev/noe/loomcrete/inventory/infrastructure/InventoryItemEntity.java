package dev.noe.loomcrete.inventory.infrastructure;

import dev.noe.loomcrete.domain.InventoryItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the domain InventoryItem record to the database.
 * Includes tenant_id index for efficient multi-tenancy queries.
 */
@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenant_id")
})
public class InventoryItemEntity {
    @Id
    @Column(name = "id", length = 36)
    public String id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    public String tenantId;

    @Column(name = "name", nullable = false, length = 255)
    public String name;

    @Column(name = "available_quantity", nullable = false)
    public int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    public int reservedQuantity;

    public InventoryItemEntity() {
    }

    public InventoryItemEntity(String id, String tenantId, String name, int availableQuantity, int reservedQuantity) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public static InventoryItemEntity from(InventoryItem domain) {
        return new InventoryItemEntity(
            domain.id(),
            domain.tenantId(),
            domain.name(),
            domain.availableQuantity(),
            domain.reservedQuantity()
        );
    }

    public InventoryItem toDomain() {
        return new InventoryItem(id, tenantId, name, availableQuantity, reservedQuantity);
    }

    public int totalQuantity() {
        return availableQuantity + reservedQuantity;
    }
}

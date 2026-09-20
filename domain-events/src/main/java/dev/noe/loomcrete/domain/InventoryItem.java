package dev.noe.loomcrete.domain;

import java.util.UUID;

public record InventoryItem(
    String id,
    String tenantId,
    String name,
    int availableQuantity,
    int reservedQuantity
) {
    public InventoryItem {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("InventoryItem id cannot be null or blank");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("InventoryItem tenantId cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("InventoryItem name cannot be null or blank");
        }
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("availableQuantity cannot be negative");
        }
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("reservedQuantity cannot be negative");
        }
    }

    public static InventoryItem create(String tenantId, String name, int availableQuantity) {
        return new InventoryItem(
            UUID.randomUUID().toString(),
            tenantId,
            name,
            availableQuantity,
            0
        );
    }

    public int totalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public boolean canReserve(int quantity) {
        return quantity > 0 && quantity <= availableQuantity;
    }

    public InventoryItem reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalArgumentException(
                "Cannot reserve " + quantity + " units; only " + availableQuantity + " available"
            );
        }
        return new InventoryItem(
            id,
            tenantId,
            name,
            availableQuantity - quantity,
            reservedQuantity + quantity
        );
    }

    public InventoryItem release(int quantity) {
        if (quantity < 0 || quantity > reservedQuantity) {
            throw new IllegalArgumentException(
                "Cannot release " + quantity + " units; only " + reservedQuantity + " reserved"
            );
        }
        return new InventoryItem(
            id,
            tenantId,
            name,
            availableQuantity + quantity,
            reservedQuantity - quantity
        );
    }
}

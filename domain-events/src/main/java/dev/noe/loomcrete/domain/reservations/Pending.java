package dev.noe.loomcrete.domain.reservations;

import java.time.Instant;
import java.util.UUID;

public record Pending(
    String id,
    String tenantId,
    String inventoryItemId,
    int quantity,
    Instant createdAt
) implements Reservation {
    public Pending {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Reservation id cannot be null or blank");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Reservation tenantId cannot be null or blank");
        }
        if (inventoryItemId == null || inventoryItemId.isBlank()) {
            throw new IllegalArgumentException("Reservation inventoryItemId cannot be null or blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Reservation createdAt cannot be null");
        }
    }

    public static Pending create(String tenantId, String inventoryItemId, int quantity) {
        return new Pending(
            UUID.randomUUID().toString(),
            tenantId,
            inventoryItemId,
            quantity,
            Instant.now()
        );
    }
}

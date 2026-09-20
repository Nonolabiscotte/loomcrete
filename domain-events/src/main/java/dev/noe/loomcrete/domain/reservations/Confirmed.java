package dev.noe.loomcrete.domain.reservations;

import java.time.Instant;

public record Confirmed(
    String id,
    String tenantId,
    String inventoryItemId,
    int quantity,
    Instant createdAt,
    Instant confirmedAt
) implements Reservation {
    public Confirmed {
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
        if (confirmedAt == null) {
            throw new IllegalArgumentException("Reservation confirmedAt cannot be null");
        }
    }
}

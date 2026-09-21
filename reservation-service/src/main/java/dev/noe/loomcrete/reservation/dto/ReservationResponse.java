package dev.noe.loomcrete.reservation.dto;

import java.time.Instant;

public record ReservationResponse(
    String id,
    String tenantId,
    String inventoryItemId,
    int quantity,
    String status,
    Instant createdAt,
    Instant confirmedAt
) {
}

package dev.noe.loomcrete.domain.events;

import java.time.Instant;

public record ReservationEvent(
    String reservationId,
    String tenantId,
    String inventoryItemId,
    int quantity,
    ReservationEventType eventType,
    Instant occurredAt
) {
    public ReservationEvent {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("reservationId cannot be null or blank");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        if (inventoryItemId == null || inventoryItemId.isBlank()) {
            throw new IllegalArgumentException("inventoryItemId cannot be null or blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType cannot be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt cannot be null");
        }
    }

    public static ReservationEvent created(String reservationId, String tenantId, String inventoryItemId, int quantity) {
        return new ReservationEvent(reservationId, tenantId, inventoryItemId, quantity, ReservationEventType.CREATED, Instant.now());
    }

    public static ReservationEvent confirmed(String reservationId, String tenantId, String inventoryItemId, int quantity) {
        return new ReservationEvent(reservationId, tenantId, inventoryItemId, quantity, ReservationEventType.CONFIRMED, Instant.now());
    }

    public static ReservationEvent expired(String reservationId, String tenantId, String inventoryItemId, int quantity) {
        return new ReservationEvent(reservationId, tenantId, inventoryItemId, quantity, ReservationEventType.EXPIRED, Instant.now());
    }

    public static ReservationEvent cancelled(String reservationId, String tenantId, String inventoryItemId, int quantity) {
        return new ReservationEvent(reservationId, tenantId, inventoryItemId, quantity, ReservationEventType.CANCELLED, Instant.now());
    }
}

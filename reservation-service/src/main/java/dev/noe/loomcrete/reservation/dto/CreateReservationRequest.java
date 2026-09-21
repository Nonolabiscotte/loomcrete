package dev.noe.loomcrete.reservation.dto;

public record CreateReservationRequest(
    String tenantId,
    String inventoryItemId,
    int quantity
) {
}

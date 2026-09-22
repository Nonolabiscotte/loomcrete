package dev.noe.loomcrete.reservation.api.dto.in;

public record CreateReservationRequest(
    String tenantId,
    String inventoryItemId,
    int quantity
) {
}

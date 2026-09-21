package dev.noe.loomcrete.reservation.infrastructure;

/**
 * Enum mapping the sealed Reservation hierarchy to database status values.
 * Ensures type-safe status representation in persistence layer.
 */
public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    EXPIRED,
    CANCELLED
}

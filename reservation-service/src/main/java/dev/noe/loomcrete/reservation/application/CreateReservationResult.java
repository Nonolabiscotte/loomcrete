package dev.noe.loomcrete.reservation.application;

public sealed interface CreateReservationResult permits
    CreateReservationSuccess, CreateReservationFailure {
}

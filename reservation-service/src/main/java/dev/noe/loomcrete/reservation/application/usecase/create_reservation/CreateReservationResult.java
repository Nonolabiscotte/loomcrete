package dev.noe.loomcrete.reservation.application.usecase.create_reservation;

public sealed interface CreateReservationResult permits
    CreateReservationSuccess, CreateReservationFailure {
}

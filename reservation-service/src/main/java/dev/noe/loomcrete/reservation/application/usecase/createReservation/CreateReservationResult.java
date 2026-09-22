package dev.noe.loomcrete.reservation.application.usecase.createReservation;

public sealed interface CreateReservationResult permits
    CreateReservationSuccess, CreateReservationFailure {
}

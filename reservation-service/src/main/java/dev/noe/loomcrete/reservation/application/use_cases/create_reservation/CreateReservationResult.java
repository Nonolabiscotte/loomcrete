package dev.noe.loomcrete.reservation.application.use_cases.create_reservation;

public sealed interface CreateReservationResult permits
    CreateReservationSuccess, CreateReservationFailure {
}

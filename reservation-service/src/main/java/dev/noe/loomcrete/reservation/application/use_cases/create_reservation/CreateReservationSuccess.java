package dev.noe.loomcrete.reservation.application.use_cases.create_reservation;

import dev.noe.loomcrete.domain.reservations.Confirmed;

public record CreateReservationSuccess(Confirmed confirmed) implements CreateReservationResult {
}

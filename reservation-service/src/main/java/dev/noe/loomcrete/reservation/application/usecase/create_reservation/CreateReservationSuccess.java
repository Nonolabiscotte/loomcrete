package dev.noe.loomcrete.reservation.application.usecase.create_reservation;

import dev.noe.loomcrete.domain.reservations.Confirmed;

public record CreateReservationSuccess(Confirmed confirmed) implements CreateReservationResult {
}

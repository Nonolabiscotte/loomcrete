package dev.noe.loomcrete.reservation.application;

import dev.noe.loomcrete.domain.reservations.Confirmed;

public record CreateReservationSuccess(Confirmed confirmed) implements CreateReservationResult {
}

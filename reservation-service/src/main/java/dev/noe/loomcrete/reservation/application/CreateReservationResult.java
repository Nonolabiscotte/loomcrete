package dev.noe.loomcrete.reservation.application;

import dev.noe.loomcrete.domain.reservations.Confirmed;

public sealed class CreateReservationResult permits CreateReservationResult.Success, CreateReservationResult.Failure {

    public static Success success(Confirmed confirmed) {
        return new Success(confirmed);
    }

    public static Failure failure(String error) {
        return new Failure(error);
    }

    public static final class Success extends CreateReservationResult {
        public final Confirmed confirmed;

        public Success(Confirmed confirmed) {
            this.confirmed = confirmed;
        }
    }

    public static final class Failure extends CreateReservationResult {
        public final String error;

        public Failure(String error) {
            this.error = error;
        }
    }
}

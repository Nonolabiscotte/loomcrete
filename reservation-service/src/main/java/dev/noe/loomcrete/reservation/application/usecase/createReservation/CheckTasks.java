package dev.noe.loomcrete.reservation.application.usecase.createReservation;

import java.util.concurrent.StructuredTaskScope;

public record CheckTasks(
    StructuredTaskScope.Subtask<Object> inventory,
    StructuredTaskScope.Subtask<Object> pricing,
    StructuredTaskScope.Subtask<Object> fraud
) {}

package dev.noe.loomcrete.domain.reservations;

import java.time.Instant;

public sealed interface Reservation permits Pending, Confirmed, Expired, Cancelled {
    String id();
    String tenantId();
    String inventoryItemId();
    int quantity();
    Instant createdAt();

    default Confirmed confirm() {
        if (this instanceof Confirmed) {
            throw new IllegalStateException("Reservation is already confirmed");
        }
        if (this instanceof Expired) {
            throw new IllegalStateException("Cannot confirm an expired reservation");
        }
        if (this instanceof Cancelled) {
            throw new IllegalStateException("Cannot confirm a cancelled reservation");
        }
        return new Confirmed(id(), tenantId(), inventoryItemId(), quantity(), createdAt(), Instant.now());
    }

    default Expired expire() {
        if (this instanceof Expired e) {
            return e;
        }
        if (this instanceof Confirmed) {
            throw new IllegalStateException("Cannot expire a confirmed reservation");
        }
        if (this instanceof Cancelled) {
            throw new IllegalStateException("Cannot expire a cancelled reservation");
        }
        return new Expired(id(), tenantId(), inventoryItemId(), quantity(), createdAt(), Instant.now());
    }

    default Cancelled cancel() {
        if (this instanceof Cancelled c) {
            return c;
        }
        return new Cancelled(id(), tenantId(), inventoryItemId(), quantity(), createdAt(), Instant.now());
    }
}

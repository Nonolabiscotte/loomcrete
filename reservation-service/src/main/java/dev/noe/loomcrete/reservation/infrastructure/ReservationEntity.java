package dev.noe.loomcrete.reservation.infrastructure;

import dev.noe.loomcrete.domain.reservations.Cancelled;
import dev.noe.loomcrete.domain.reservations.Confirmed;
import dev.noe.loomcrete.domain.reservations.Expired;
import dev.noe.loomcrete.domain.reservations.Pending;
import dev.noe.loomcrete.domain.reservations.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity mapping the sealed Reservation hierarchy to a single database table.
 * Uses a single `status` column to track state (PENDING, CONFIRMED, EXPIRED, CANCELLED)
 * and nullable timestamp columns for state-specific data (confirmedAt, expiredAt, cancelledAt).
 *
 * This design choice prioritizes simpler queries over object-oriented inheritance:
 * - Single table scan for queries (efficient multi-tenancy filtering)
 * - No join complexity or discriminator columns
 * - Nullable columns are acceptable since only one state is active per row
 */
@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_tenant_status", columnList = "tenant_id, status")
})
public class ReservationEntity {
    @Id
    @Column(name = "id", length = 36)
    public String id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    public String tenantId;

    @Column(name = "inventory_item_id", nullable = false, length = 36)
    public String inventoryItemId;

    @Column(name = "quantity", nullable = false)
    public int quantity;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "confirmed_at")
    public Instant confirmedAt;

    @Column(name = "expired_at")
    public Instant expiredAt;

    @Column(name = "cancelled_at")
    public Instant cancelledAt;

    public ReservationEntity() {
    }

    public ReservationEntity(String id, String tenantId, String inventoryItemId, int quantity,
                           ReservationStatus status, Instant createdAt, Instant confirmedAt,
                           Instant expiredAt, Instant cancelledAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.inventoryItemId = inventoryItemId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.expiredAt = expiredAt;
        this.cancelledAt = cancelledAt;
    }

    /**
     * Convert domain Reservation (sealed type) to entity.
     * Uses pattern matching to extract state-specific fields.
     */
    public static ReservationEntity from(Reservation domain) {
        return switch (domain) {
            case Pending p -> new ReservationEntity(
                p.id(), p.tenantId(), p.inventoryItemId(), p.quantity(),
                ReservationStatus.PENDING, p.createdAt(), null, null, null
            );
            case Confirmed c -> new ReservationEntity(
                c.id(), c.tenantId(), c.inventoryItemId(), c.quantity(),
                ReservationStatus.CONFIRMED, c.createdAt(), c.confirmedAt(), null, null
            );
            case Expired e -> new ReservationEntity(
                e.id(), e.tenantId(), e.inventoryItemId(), e.quantity(),
                ReservationStatus.EXPIRED, e.createdAt(), null, e.expiredAt(), null
            );
            case Cancelled ca -> new ReservationEntity(
                ca.id(), ca.tenantId(), ca.inventoryItemId(), ca.quantity(),
                ReservationStatus.CANCELLED, ca.createdAt(), null, null, ca.cancelledAt()
            );
        };
    }

    /**
     * Convert entity back to domain Reservation (sealed type).
     * Reconstructs the appropriate sealed type based on status column.
     */
    public Reservation toDomain() {
        return switch (status) {
            case PENDING -> new Pending(id, tenantId, inventoryItemId, quantity, createdAt);
            case CONFIRMED -> new Confirmed(id, tenantId, inventoryItemId, quantity, createdAt, confirmedAt);
            case EXPIRED -> new Expired(id, tenantId, inventoryItemId, quantity, createdAt, expiredAt);
            case CANCELLED -> new Cancelled(id, tenantId, inventoryItemId, quantity, createdAt, cancelledAt);
        };
    }
}

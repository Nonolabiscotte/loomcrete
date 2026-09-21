package dev.noe.loomcrete.reservation.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Panache repository for Reservation persistence.
 * Provides CRUD operations and query methods for ReservationEntity.
 * Supports multi-tenancy with findByTenantId() and status filtering with findByStatus().
 */
@ApplicationScoped
public class ReservationRepository implements PanacheRepositoryBase<ReservationEntity, String> {

    /**
     * Find all reservations for a given tenant.
     * Critical for multi-tenancy isolation.
     */
    public List<ReservationEntity> findByTenantId(String tenantId) {
        return find("tenantId", tenantId).list();
    }

    /**
     * Find all reservations with a specific status.
     * Useful for scheduled jobs (e.g., expiry sweep) and status-based queries.
     */
    public List<ReservationEntity> findByStatus(ReservationStatus status) {
        return find("status", status).list();
    }

    /**
     * Find all reservations for a tenant with a specific status.
     * Combines multi-tenancy and status filtering for precise queries.
     */
    public List<ReservationEntity> findByTenantIdAndStatus(String tenantId, ReservationStatus status) {
        return find("tenantId = ?1 and status = ?2", tenantId, status).list();
    }
}

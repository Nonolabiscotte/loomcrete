package dev.noe.loomcrete.reservation.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import dev.noe.loomcrete.reservation.domain.ReservationStatus;
import java.util.List;

@ApplicationScoped
class ReservationJpaRepository implements PanacheRepositoryBase<ReservationEntity, String> {

    List<ReservationEntity> findByTenantId(String tenantId) {
        return find("tenantId", tenantId).list();
    }

    List<ReservationEntity> findByStatus(ReservationStatus status) {
        return find("status", status).list();
    }

    List<ReservationEntity> findByTenantIdAndStatus(String tenantId, ReservationStatus status) {
        return find("tenantId = ?1 and status = ?2", tenantId, status).list();
    }
}

package dev.noe.loomcrete.reservation.domain;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Optional<Object> findByIdOptional(String id);
    List<Object> findByTenantId(String tenantId);
    List<Object> findByTenantIdAndStatus(String tenantId, ReservationStatus status);
    List<Object> findByStatus(ReservationStatus status);
    void persist(Object entity);
    void deleteById(String id);
}

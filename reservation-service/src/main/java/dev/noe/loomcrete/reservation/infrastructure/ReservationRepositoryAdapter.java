package dev.noe.loomcrete.reservation.infrastructure;

import dev.noe.loomcrete.reservation.domain.ReservationRepository;
import dev.noe.loomcrete.reservation.domain.ReservationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReservationRepositoryAdapter implements ReservationRepository {

    @Inject
    ReservationJpaRepository jpaRepository;

    @Override
    public Optional<Object> findByIdOptional(String id) {
        return jpaRepository.findByIdOptional(id).map(entity -> (Object) entity);
    }

    @Override
    public List<Object> findByTenantId(String tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
            .map(entity -> (Object) entity)
            .toList();
    }

    @Override
    public List<Object> findByTenantIdAndStatus(String tenantId, ReservationStatus status) {
        return jpaRepository.findByTenantIdAndStatus(tenantId, status).stream()
            .map(entity -> (Object) entity)
            .toList();
    }

    @Override
    public List<Object> findByStatus(ReservationStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(entity -> (Object) entity)
            .toList();
    }

    @Override
    public void persist(Object entity) {
        jpaRepository.persist((ReservationEntity) entity);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}

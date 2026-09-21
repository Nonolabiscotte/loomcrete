package dev.noe.loomcrete.reservation.service;

import dev.noe.loomcrete.reservation.infrastructure.ReservationEntity;
import dev.noe.loomcrete.reservation.infrastructure.ReservationRepository;
import dev.noe.loomcrete.reservation.infrastructure.ReservationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Reservation business logic.
 *
 * Quarkus 3.8.6 automatically dispatches blocking JDBC calls to virtual threads.
 * No explicit annotation needed — this is the default behavior when using Panache
 * repositories with JDBC. This unlocks high concurrency without manual thread-pool
 * tuning (the core advantage of Java 21 virtual threads).
 *
 * Methods log the current thread name/ID to verify virtual thread execution.
 * Virtual threads appear as carriers from the ForkJoinPool (e.g., "ForkJoinPool.commonPool-worker-N").
 */
@ApplicationScoped
public class ReservationService {
    private static final Logger LOG = Logger.getLogger(ReservationService.class);

    @Inject
    ReservationRepository reservationRepository;

    /**
     * Retrieve a reservation by ID.
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public Optional<ReservationEntity> getReservationById(String id) {
        logThreadInfo("getReservationById");
        return reservationRepository.findByIdOptional(id);
    }

    /**
     * List all reservations for a given tenant.
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public List<ReservationEntity> listReservationsByTenant(String tenantId) {
        logThreadInfo("listReservationsByTenant");
        return reservationRepository.findByTenantId(tenantId);
    }

    /**
     * List reservations for a tenant with a specific status.
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public List<ReservationEntity> listReservationsByTenantAndStatus(String tenantId, ReservationStatus status) {
        logThreadInfo("listReservationsByTenantAndStatus");
        return reservationRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Find all reservations with a specific status (useful for background jobs).
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public List<ReservationEntity> listReservationsByStatus(ReservationStatus status) {
        logThreadInfo("listReservationsByStatus");
        return reservationRepository.findByStatus(status);
    }

    /**
     * Persist a reservation entity.
     * JDBC write is automatically dispatched to a virtual thread by Quarkus.
     */
    public void persistReservation(ReservationEntity entity) {
        logThreadInfo("persistReservation");
        reservationRepository.persist(entity);
    }

    /**
     * Delete a reservation by ID.
     * JDBC delete is automatically dispatched to a virtual thread by Quarkus.
     */
    public void deleteReservationById(String id) {
        logThreadInfo("deleteReservationById");
        reservationRepository.deleteById(id);
    }

    private void logThreadInfo(String methodName) {
        Thread currentThread = Thread.currentThread();
        String threadInfo = String.format(
            "method=%s thread=%s isVirtual=%s",
            methodName,
            currentThread.getName(),
            currentThread.isVirtual()
        );
        LOG.debug(threadInfo);
    }
}

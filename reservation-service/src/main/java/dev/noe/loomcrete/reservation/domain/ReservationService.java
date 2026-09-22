package dev.noe.loomcrete.reservation.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReservationService {
    private static final Logger LOG = Logger.getLogger(ReservationService.class);

    @Inject
    ReservationRepository reservationRepository;

    public Optional<Object> getReservationById(String id) {
        logThreadInfo("getReservationById");
        return reservationRepository.findByIdOptional(id);
    }

    public List<Object> listReservationsByTenant(String tenantId) {
        logThreadInfo("listReservationsByTenant");
        return reservationRepository.findByTenantId(tenantId);
    }

    public List<Object> listReservationsByTenantAndStatus(String tenantId, ReservationStatus status) {
        logThreadInfo("listReservationsByTenantAndStatus");
        return reservationRepository.findByTenantIdAndStatus(tenantId, status);
    }

    public List<Object> listReservationsByStatus(ReservationStatus status) {
        logThreadInfo("listReservationsByStatus");
        return reservationRepository.findByStatus(status);
    }

    public void persistReservation(Object entity) {
        logThreadInfo("persistReservation");
        reservationRepository.persist(entity);
    }

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

package dev.noe.loomcrete.reservation;

import dev.noe.loomcrete.domain.reservations.Confirmed;
import dev.noe.loomcrete.domain.reservations.Pending;
import dev.noe.loomcrete.inventory.service.InventoryService;
import dev.noe.loomcrete.reservation.dto.CreateReservationRequest;
import dev.noe.loomcrete.reservation.dto.ReservationResponse;
import dev.noe.loomcrete.reservation.infrastructure.ReservationEntity;
import dev.noe.loomcrete.reservation.service.ReservationService;
import dev.noe.loomcrete.reservation.task.FraudCheckTask;
import dev.noe.loomcrete.reservation.task.FraudCheckResult;
import dev.noe.loomcrete.reservation.task.InventoryCheckTask;
import dev.noe.loomcrete.reservation.task.InventoryCheckResult;
import dev.noe.loomcrete.reservation.task.PricingCheckTask;
import dev.noe.loomcrete.reservation.task.PricingCheckResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {
    private static final Logger LOG = Logger.getLogger(ReservationResource.class);

    @Inject
    ReservationService reservationService;

    @Inject
    InventoryService inventoryService;

    @POST
    @Transactional
    public Response createReservation(CreateReservationRequest request) {
        LOG.infof("Creating reservation: tenant=%s, item=%s, quantity=%d",
            request.tenantId(), request.inventoryItemId(), request.quantity());

        if (request.tenantId() == null || request.tenantId().isEmpty()) {
            return errorResponse(400, "Tenant ID is required");
        }
        if (request.inventoryItemId() == null || request.inventoryItemId().isEmpty()) {
            return errorResponse(400, "Inventory item ID is required");
        }
        if (request.quantity() <= 0) {
            return errorResponse(400, "Quantity must be positive");
        }

        try (var scope = new StructuredTaskScope<Object>()) {
            var inventoryTask = scope.fork(new InventoryCheckTask(
                inventoryService,
                request.inventoryItemId(),
                request.quantity()
            ));
            var pricingTask = scope.fork(new PricingCheckTask(request.quantity()));
            var fraudTask = scope.fork(new FraudCheckTask(request.tenantId()));

            try {
                scope.joinUntil(Instant.now().plusSeconds(5));
            } catch (InterruptedException e) {
                LOG.errorf("Structured task scope interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
                return errorResponse(500, "Request was interrupted");
            } catch (TimeoutException e) {
                LOG.errorf("Reservation checks timed out: %s", e.getMessage());
                return errorResponse(500, "Reservation checks timed out");
            }

            // Check inventory
            if (inventoryTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = inventoryTask.exception();
                LOG.errorf("Inventory check failed: %s", ex.getMessage());
                return errorResponse(400, "Inventory check failed");
            }
            Object inventoryObj = inventoryTask.get();
            InventoryCheckResult inventoryResult = (InventoryCheckResult) inventoryObj;
            if (!inventoryResult.success()) {
                LOG.warnf("Inventory check failed: %s", inventoryResult.details());
                return errorResponse(400, inventoryResult.details());
            }

            // Check pricing
            if (pricingTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = pricingTask.exception();
                LOG.errorf("Pricing check failed: %s", ex.getMessage());
                return errorResponse(400, "Pricing check failed");
            }
            Object pricingObj = pricingTask.get();
            PricingCheckResult pricingResult = (PricingCheckResult) pricingObj;
            if (!pricingResult.success()) {
                LOG.warnf("Pricing check failed: %s", pricingResult.details());
                return errorResponse(400, (String) pricingResult.details());
            }

            // Check fraud
            if (fraudTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = fraudTask.exception();
                LOG.errorf("Fraud check failed: %s", ex.getMessage());
                return errorResponse(400, "Fraud check failed");
            }
            Object fraudObj = fraudTask.get();
            FraudCheckResult fraudResult = (FraudCheckResult) fraudObj;
            if (!fraudResult.success()) {
                LOG.warnf("Fraud check failed: %s", fraudResult.details());
                return errorResponse(400, fraudResult.details());
            }

            String reservationId = UUID.randomUUID().toString();
            Instant now = Instant.now();

            Pending pending = new Pending(
                reservationId,
                request.tenantId(),
                request.inventoryItemId(),
                request.quantity(),
                now
            );

            Confirmed confirmed = pending.confirm();
            ReservationEntity entity = ReservationEntity.from(confirmed);
            reservationService.persistReservation(entity);

            ReservationResponse response = new ReservationResponse(
                confirmed.id(),
                confirmed.tenantId(),
                confirmed.inventoryItemId(),
                confirmed.quantity(),
                "CONFIRMED",
                confirmed.createdAt(),
                confirmed.confirmedAt()
            );

            LOG.infof("Reservation created successfully: id=%s", reservationId);
            return Response.status(201).entity(response).build();
        }
    }

    private Response errorResponse(int status, String message) {
        return Response.status(status)
            .entity(new ErrorResponse(message))
            .build();
    }

    record ErrorResponse(String error) {
    }
}

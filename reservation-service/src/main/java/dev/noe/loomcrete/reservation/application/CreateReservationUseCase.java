package dev.noe.loomcrete.reservation.application;

import dev.noe.loomcrete.domain.reservations.Confirmed;
import dev.noe.loomcrete.domain.reservations.Pending;
import dev.noe.loomcrete.inventory.service.InventoryService;
import dev.noe.loomcrete.reservation.domain.ReservationService;
import dev.noe.loomcrete.reservation.infrastructure.ReservationEntity;
import dev.noe.loomcrete.reservation.task.FraudCheckTask;
import dev.noe.loomcrete.reservation.task.FraudCheckResult;
import dev.noe.loomcrete.reservation.task.InventoryCheckTask;
import dev.noe.loomcrete.reservation.task.InventoryCheckResult;
import dev.noe.loomcrete.reservation.task.PricingCheckTask;
import dev.noe.loomcrete.reservation.task.PricingCheckResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class CreateReservationUseCase {
    private static final Logger LOG = Logger.getLogger(CreateReservationUseCase.class);

    @Inject
    ReservationService reservationService;

    @Inject
    InventoryService inventoryService;

    public CreateReservationResult execute(String tenantId, String inventoryItemId, int quantity) {
        LOG.infof("Creating reservation: tenant=%s, item=%s, quantity=%d",
            tenantId, inventoryItemId, quantity);

        // Validate input
        if (tenantId == null || tenantId.isEmpty()) {
            return CreateReservationResult.failure("Tenant ID is required");
        }
        if (inventoryItemId == null || inventoryItemId.isEmpty()) {
            return CreateReservationResult.failure("Inventory item ID is required");
        }
        if (quantity <= 0) {
            return CreateReservationResult.failure("Quantity must be positive");
        }

        // Run parallel checks using structured concurrency
        try (var scope = new StructuredTaskScope<Object>()) {
            var inventoryTask = scope.fork(new InventoryCheckTask(
                inventoryService,
                inventoryItemId,
                quantity
            ));
            var pricingTask = scope.fork(new PricingCheckTask(quantity));
            var fraudTask = scope.fork(new FraudCheckTask(tenantId));

            try {
                scope.joinUntil(Instant.now().plusSeconds(5));
            } catch (InterruptedException e) {
                LOG.errorf("Structured task scope interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
                return CreateReservationResult.failure("Request was interrupted");
            } catch (TimeoutException e) {
                LOG.errorf("Reservation checks timed out: %s", e.getMessage());
                return CreateReservationResult.failure("Reservation checks timed out");
            }

            // Check inventory
            if (inventoryTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = inventoryTask.exception();
                LOG.errorf("Inventory check failed: %s", ex.getMessage());
                return CreateReservationResult.failure("Inventory check failed");
            }
            Object inventoryObj = inventoryTask.get();
            InventoryCheckResult inventoryResult = (InventoryCheckResult) inventoryObj;
            if (!inventoryResult.success()) {
                LOG.warnf("Inventory check failed: %s", inventoryResult.details());
                return CreateReservationResult.failure(inventoryResult.details());
            }

            // Check pricing
            if (pricingTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = pricingTask.exception();
                LOG.errorf("Pricing check failed: %s", ex.getMessage());
                return CreateReservationResult.failure("Pricing check failed");
            }
            Object pricingObj = pricingTask.get();
            PricingCheckResult pricingResult = (PricingCheckResult) pricingObj;
            if (!pricingResult.success()) {
                LOG.warnf("Pricing check failed: %s", pricingResult.details());
                return CreateReservationResult.failure((String) pricingResult.details());
            }

            // Check fraud
            if (fraudTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = fraudTask.exception();
                LOG.errorf("Fraud check failed: %s", ex.getMessage());
                return CreateReservationResult.failure("Fraud check failed");
            }
            Object fraudObj = fraudTask.get();
            FraudCheckResult fraudResult = (FraudCheckResult) fraudObj;
            if (!fraudResult.success()) {
                LOG.warnf("Fraud check failed: %s", fraudResult.details());
                return CreateReservationResult.failure(fraudResult.details());
            }

            // All checks passed, create reservation
            String reservationId = UUID.randomUUID().toString();
            Instant now = Instant.now();

            Pending pending = new Pending(
                reservationId,
                tenantId,
                inventoryItemId,
                quantity,
                now
            );

            Confirmed confirmed = pending.confirm();
            ReservationEntity entity = ReservationEntity.from(confirmed);
            reservationService.persistReservation(entity);

            LOG.infof("Reservation created successfully: id=%s", reservationId);
            return CreateReservationResult.success(confirmed);
        }
    }
}

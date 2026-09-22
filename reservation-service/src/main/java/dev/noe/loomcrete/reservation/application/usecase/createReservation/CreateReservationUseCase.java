package dev.noe.loomcrete.reservation.application.usecase.createReservation;

import dev.noe.loomcrete.domain.reservations.Confirmed;
import dev.noe.loomcrete.domain.reservations.Pending;
import dev.noe.loomcrete.inventory.service.InventoryService;
import dev.noe.loomcrete.reservation.domain.ReservationService;
import dev.noe.loomcrete.reservation.infrastructure.ReservationEntity;
import dev.noe.loomcrete.reservation.application.checks.fraud.FraudDetectionCheck;
import dev.noe.loomcrete.reservation.application.checks.fraud.FraudCheckResult;
import dev.noe.loomcrete.reservation.application.checks.inventory.InventoryAvailabilityCheck;
import dev.noe.loomcrete.reservation.application.checks.inventory.InventoryCheckResult;
import dev.noe.loomcrete.reservation.application.checks.pricing.PricingValidationCheck;
import dev.noe.loomcrete.reservation.application.checks.pricing.PricingCheckResult;
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
            return new CreateReservationFailure("Tenant ID is required");
        }
        if (inventoryItemId == null || inventoryItemId.isEmpty()) {
            return new CreateReservationFailure("Inventory item ID is required");
        }
        if (quantity <= 0) {
            return new CreateReservationFailure("Quantity must be positive");
        }

        // Run parallel checks using structured concurrency
        try (var scope = new StructuredTaskScope<Object>()) {
            var inventoryTask = scope.fork(new InventoryAvailabilityCheck(
                inventoryService,
                inventoryItemId,
                quantity
            ));
            var pricingTask = scope.fork(new PricingValidationCheck(quantity));
            var fraudTask = scope.fork(new FraudDetectionCheck(tenantId));

            try {
                scope.joinUntil(Instant.now().plusSeconds(5));
            } catch (InterruptedException e) {
                LOG.errorf("Structured task scope interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
                return new CreateReservationFailure("Request was interrupted");
            } catch (TimeoutException e) {
                LOG.errorf("Reservation checks timed out: %s", e.getMessage());
                return new CreateReservationFailure("Reservation checks timed out");
            }

            // Check inventory
            if (inventoryTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = inventoryTask.exception();
                LOG.errorf("Inventory check failed: %s", ex.getMessage());
                return new CreateReservationFailure("Inventory check failed");
            }
            Object inventoryObj = inventoryTask.get();
            InventoryCheckResult inventoryResult = (InventoryCheckResult) inventoryObj;
            if (!inventoryResult.success()) {
                LOG.warnf("Inventory check failed: %s", inventoryResult.details());
                return new CreateReservationFailure(inventoryResult.details());
            }

            // Check pricing
            if (pricingTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = pricingTask.exception();
                LOG.errorf("Pricing check failed: %s", ex.getMessage());
                return new CreateReservationFailure("Pricing check failed");
            }
            Object pricingObj = pricingTask.get();
            PricingCheckResult pricingResult = (PricingCheckResult) pricingObj;
            if (!pricingResult.success()) {
                LOG.warnf("Pricing check failed: %s", pricingResult.details());
                return new CreateReservationFailure((String) pricingResult.details());
            }

            // Check fraud
            if (fraudTask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                Throwable ex = fraudTask.exception();
                LOG.errorf("Fraud check failed: %s", ex.getMessage());
                return new CreateReservationFailure("Fraud check failed");
            }
            Object fraudObj = fraudTask.get();
            FraudCheckResult fraudResult = (FraudCheckResult) fraudObj;
            if (!fraudResult.success()) {
                LOG.warnf("Fraud check failed: %s", fraudResult.details());
                return new CreateReservationFailure(fraudResult.details());
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
            return new CreateReservationSuccess(confirmed);
        }
    }
}

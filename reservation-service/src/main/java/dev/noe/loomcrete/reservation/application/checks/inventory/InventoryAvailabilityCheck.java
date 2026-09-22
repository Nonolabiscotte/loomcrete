package dev.noe.loomcrete.reservation.application.checks.inventory;

import dev.noe.loomcrete.inventory.infrastructure.InventoryItemEntity;
import dev.noe.loomcrete.inventory.service.InventoryService;
import io.quarkus.arc.Arc;
import java.util.Optional;
import java.util.concurrent.Callable;

public record InventoryAvailabilityCheck(
    InventoryService inventoryService,
    String itemId,
    int requestedQuantity
) implements Callable<Object> {

    @Override
    public Object call() throws Exception {
        var requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            Optional<InventoryItemEntity> item = inventoryService.getInventoryItemById(itemId);

            if (item.isEmpty()) {
                return new InventoryCheckResult(false, "Inventory item not found: " + itemId);
            }

            InventoryItemEntity inventoryItem = item.get();
            if (inventoryItem.availableQuantity < requestedQuantity) {
                return new InventoryCheckResult(
                    false,
                    "Insufficient inventory: requested " + requestedQuantity +
                        ", available " + inventoryItem.availableQuantity
                );
            }

            return new InventoryCheckResult(true, inventoryItem.id);
        } finally {
            requestContext.deactivate();
        }
    }
}

package dev.noe.loomcrete.reservation.application.checks.fraud;

import java.util.concurrent.Callable;

public record FraudDetectionCheck(String tenantId) implements Callable<Object> {

    @Override
    public Object call() throws Exception {
        if (tenantId == null || tenantId.isEmpty()) {
            return new FraudCheckResult(false, "Tenant ID cannot be empty");
        }

        if (tenantId.startsWith("blocked_")) {
            return new FraudCheckResult(false, "Tenant blocked by fraud detection system");
        }

        return new FraudCheckResult(true, "Fraud check passed");
    }
}

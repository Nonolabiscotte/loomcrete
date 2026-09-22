package dev.noe.loomcrete.reservation.application.checks.pricing;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

public record PricingValidationCheck(int quantity) implements Callable<Object> {

    @Override
    public Object call() throws Exception {
        if (quantity <= 0) {
            return new PricingCheckResult(false, "Quantity must be positive");
        }

        BigDecimal unitPrice = new BigDecimal("10.00");
        BigDecimal discount = quantity >= 10 ? new BigDecimal("0.10") : BigDecimal.ZERO;
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal totalPrice = subtotal.multiply(BigDecimal.ONE.subtract(discount));

        return new PricingCheckResult(true, totalPrice);
    }
}

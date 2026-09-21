package dev.noe.loomcrete.reservation.task;

import java.math.BigDecimal;

public record PricingCheckResult(boolean success, Object details) {
}

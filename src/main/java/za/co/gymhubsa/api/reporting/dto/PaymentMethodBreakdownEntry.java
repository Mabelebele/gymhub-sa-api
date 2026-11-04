package za.co.gymhubsa.api.reporting.dto;

import za.co.gymhubsa.api.payment.entity.PaymentMethod;

import java.math.BigDecimal;

public record PaymentMethodBreakdownEntry(
        PaymentMethod method,
        BigDecimal totalAmount,
        long count
) {
}

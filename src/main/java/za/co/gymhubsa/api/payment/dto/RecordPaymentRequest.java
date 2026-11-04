package za.co.gymhubsa.api.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;

import java.math.BigDecimal;

public record RecordPaymentRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        Long membershipId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        String notes
) {
}

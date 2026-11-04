package za.co.gymhubsa.api.membership.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MembershipPlanRequest(
        @NotBlank(message = "Plan name is required")
        String name,

        String description,

        @NotNull(message = "Duration in days is required")
        @Positive(message = "Duration must be a positive number of days")
        Integer durationDays,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
        BigDecimal price
) {
}

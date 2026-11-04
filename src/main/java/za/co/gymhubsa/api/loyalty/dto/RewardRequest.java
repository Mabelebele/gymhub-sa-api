package za.co.gymhubsa.api.loyalty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RewardRequest(
        @NotBlank(message = "Reward name is required")
        String name,

        String description,

        @NotNull(message = "Points cost is required")
        @Positive(message = "Points cost must be positive")
        Integer pointsCost,

        @PositiveOrZero(message = "Stock quantity cannot be negative")
        Integer stockQuantity
) {
}

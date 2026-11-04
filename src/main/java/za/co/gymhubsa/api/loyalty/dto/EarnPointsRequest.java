package za.co.gymhubsa.api.loyalty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EarnPointsRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Points is required")
        @Positive(message = "Points must be positive")
        Integer points,

        @NotBlank(message = "Reason is required")
        String reason
) {
}

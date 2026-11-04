package za.co.gymhubsa.api.diet.dto;

import jakarta.validation.constraints.NotNull;

public record AssignDietPlanRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Diet plan id is required")
        Long dietPlanId
) {
}

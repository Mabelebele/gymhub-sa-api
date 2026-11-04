package za.co.gymhubsa.api.membership.dto;

import jakarta.validation.constraints.NotNull;

public record ChangePlanRequest(
        @NotNull(message = "New plan id is required")
        Long newPlanId
) {
}

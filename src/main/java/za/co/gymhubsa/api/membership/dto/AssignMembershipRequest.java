package za.co.gymhubsa.api.membership.dto;

import jakarta.validation.constraints.NotNull;

public record AssignMembershipRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Plan id is required")
        Long planId
) {
}

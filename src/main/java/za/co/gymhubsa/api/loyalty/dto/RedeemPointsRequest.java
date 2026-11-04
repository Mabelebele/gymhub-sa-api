package za.co.gymhubsa.api.loyalty.dto;

import jakarta.validation.constraints.NotNull;

public record RedeemPointsRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Reward id is required")
        Long rewardId
) {
}

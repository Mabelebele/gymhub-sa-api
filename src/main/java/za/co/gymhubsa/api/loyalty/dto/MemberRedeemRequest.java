package za.co.gymhubsa.api.loyalty.dto;

import jakarta.validation.constraints.NotNull;

public record MemberRedeemRequest(
        @NotNull(message = "Reward id is required")
        Long rewardId
) {
}

package za.co.gymhubsa.api.trainer.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTrainerRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Trainer id is required")
        Long trainerId
) {
}

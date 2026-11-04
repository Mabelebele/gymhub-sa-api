package za.co.gymhubsa.api.trainer.dto;

import jakarta.validation.constraints.NotNull;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;

public record TrainerStatusUpdateRequest(
        @NotNull(message = "Status is required")
        TrainerStatus status
) {
}

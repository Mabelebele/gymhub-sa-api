package za.co.gymhubsa.api.trainer.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerUpdateRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String phoneNumber,
        String specialization,
        String bio,
        Integer yearsOfExperience
) {
}

package za.co.gymhubsa.api.workout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkoutExerciseRequest(
        @NotBlank(message = "Exercise name is required")
        String name,

        @NotNull(message = "Sets is required")
        @Positive(message = "Sets must be positive")
        Integer sets,

        @NotNull(message = "Reps is required")
        @Positive(message = "Reps must be positive")
        Integer reps,

        Integer restSeconds,

        @NotNull(message = "Sort order is required")
        Integer sortOrder,

        String notes
) {
}

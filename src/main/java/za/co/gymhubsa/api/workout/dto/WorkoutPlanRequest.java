package za.co.gymhubsa.api.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import za.co.gymhubsa.api.workout.entity.DifficultyLevel;

import java.util.List;

public record WorkoutPlanRequest(
        @NotBlank(message = "Plan name is required")
        String name,

        String description,
        DifficultyLevel difficultyLevel,
        Integer durationWeeks,

        @NotEmpty(message = "At least one exercise is required")
        @Valid
        List<WorkoutExerciseRequest> exercises
) {
}

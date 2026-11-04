package za.co.gymhubsa.api.workout.dto;

import jakarta.validation.constraints.NotNull;

public record AssignWorkoutPlanRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Workout plan id is required")
        Long workoutPlanId
) {
}

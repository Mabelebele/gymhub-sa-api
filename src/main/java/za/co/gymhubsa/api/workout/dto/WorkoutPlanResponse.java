package za.co.gymhubsa.api.workout.dto;

import za.co.gymhubsa.api.workout.entity.DifficultyLevel;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;

import java.util.List;

public record WorkoutPlanResponse(
        Long id,
        String name,
        String description,
        DifficultyLevel difficultyLevel,
        Integer durationWeeks,
        boolean active,
        List<WorkoutExerciseResponse> exercises
) {
    public static WorkoutPlanResponse from(WorkoutPlan plan) {
        List<WorkoutExerciseResponse> exercises = plan.getExercises().stream()
                .map(WorkoutExerciseResponse::from)
                .toList();

        return new WorkoutPlanResponse(plan.getId(), plan.getName(), plan.getDescription(),
                plan.getDifficultyLevel(), plan.getDurationWeeks(), plan.isActive(), exercises);
    }

    /** Lightweight variant without the exercise list, for list/search views. */
    public static WorkoutPlanResponse summary(WorkoutPlan plan) {
        return new WorkoutPlanResponse(plan.getId(), plan.getName(), plan.getDescription(),
                plan.getDifficultyLevel(), plan.getDurationWeeks(), plan.isActive(), List.of());
    }
}


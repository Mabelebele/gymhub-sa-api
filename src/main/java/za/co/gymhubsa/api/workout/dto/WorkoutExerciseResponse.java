package za.co.gymhubsa.api.workout.dto;

import za.co.gymhubsa.api.workout.entity.WorkoutExercise;

public record WorkoutExerciseResponse(
        Long id,
        String name,
        Integer sets,
        Integer reps,
        Integer restSeconds,
        Integer sortOrder,
        String notes
) {
    public static WorkoutExerciseResponse from(WorkoutExercise exercise) {
        return new WorkoutExerciseResponse(exercise.getId(), exercise.getName(), exercise.getSets(),
                exercise.getReps(), exercise.getRestSeconds(), exercise.getSortOrder(), exercise.getNotes());
    }
}

package za.co.gymhubsa.api.workout.dto;

import za.co.gymhubsa.api.workout.entity.WorkoutProgressLog;

import java.time.LocalDate;

public record WorkoutProgressLogResponse(
        Long id,
        Long memberWorkoutPlanId,
        LocalDate logDate,
        Integer durationMinutes,
        Integer rating,
        String notes
) {
    public static WorkoutProgressLogResponse from(WorkoutProgressLog log) {
        return new WorkoutProgressLogResponse(log.getId(), log.getMemberWorkoutPlan().getId(), log.getLogDate(),
                log.getDurationMinutes(), log.getRating(), log.getNotes());
    }
}

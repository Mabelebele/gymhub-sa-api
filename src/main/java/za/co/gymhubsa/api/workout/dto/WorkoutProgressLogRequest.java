package za.co.gymhubsa.api.workout.dto;

import java.time.LocalDate;

public record WorkoutProgressLogRequest(
        LocalDate logDate,
        Integer durationMinutes,
        Integer rating,
        String notes
) {
}

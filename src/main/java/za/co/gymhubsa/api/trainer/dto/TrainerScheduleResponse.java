package za.co.gymhubsa.api.trainer.dto;

import za.co.gymhubsa.api.trainer.entity.TrainerSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record TrainerScheduleResponse(
        Long id,
        Long trainerId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
    public static TrainerScheduleResponse from(TrainerSchedule schedule) {
        return new TrainerScheduleResponse(schedule.getId(), schedule.getTrainer().getId(),
                schedule.getDayOfWeek(), schedule.getStartTime(), schedule.getEndTime());
    }
}

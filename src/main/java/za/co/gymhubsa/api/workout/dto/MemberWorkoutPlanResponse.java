package za.co.gymhubsa.api.workout.dto;

import za.co.gymhubsa.api.workout.entity.MemberWorkoutPlan;
import za.co.gymhubsa.api.workout.entity.WorkoutAssignmentStatus;

import java.time.LocalDate;

public record MemberWorkoutPlanResponse(
        Long id,
        Long memberId,
        String memberFullName,
        Long workoutPlanId,
        String workoutPlanName,
        LocalDate assignedDate,
        WorkoutAssignmentStatus status
) {
    public static MemberWorkoutPlanResponse from(MemberWorkoutPlan assignment) {
        return new MemberWorkoutPlanResponse(
                assignment.getId(),
                assignment.getMember().getId(),
                assignment.getMember().getUser().getFirstName() + " " + assignment.getMember().getUser().getLastName(),
                assignment.getWorkoutPlan().getId(),
                assignment.getWorkoutPlan().getName(),
                assignment.getAssignedDate(),
                assignment.getStatus()
        );
    }
}

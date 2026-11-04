package za.co.gymhubsa.api.trainer.dto;

import za.co.gymhubsa.api.trainer.entity.MemberTrainerAssignment;

import java.time.LocalDate;

public record MemberTrainerAssignmentResponse(
        Long id,
        Long memberId,
        String memberFullName,
        Long trainerId,
        String trainerFullName,
        LocalDate assignedDate,
        LocalDate endDate,
        boolean active
) {
    public static MemberTrainerAssignmentResponse from(MemberTrainerAssignment assignment) {
        return new MemberTrainerAssignmentResponse(
                assignment.getId(),
                assignment.getMember().getId(),
                assignment.getMember().getUser().getFirstName() + " " + assignment.getMember().getUser().getLastName(),
                assignment.getTrainer().getId(),
                assignment.getTrainer().getUser().getFirstName() + " " + assignment.getTrainer().getUser().getLastName(),
                assignment.getAssignedDate(),
                assignment.getEndDate(),
                assignment.isActive()
        );
    }
}

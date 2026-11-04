package za.co.gymhubsa.api.diet.dto;

import za.co.gymhubsa.api.diet.entity.DietAssignmentStatus;
import za.co.gymhubsa.api.diet.entity.MemberDietPlan;

import java.time.LocalDate;

public record MemberDietPlanResponse(
        Long id,
        Long memberId,
        String memberFullName,
        Long dietPlanId,
        String dietPlanName,
        LocalDate assignedDate,
        DietAssignmentStatus status
) {
    public static MemberDietPlanResponse from(MemberDietPlan assignment) {
        return new MemberDietPlanResponse(
                assignment.getId(),
                assignment.getMember().getId(),
                assignment.getMember().getUser().getFirstName() + " " + assignment.getMember().getUser().getLastName(),
                assignment.getDietPlan().getId(),
                assignment.getDietPlan().getName(),
                assignment.getAssignedDate(),
                assignment.getStatus()
        );
    }
}

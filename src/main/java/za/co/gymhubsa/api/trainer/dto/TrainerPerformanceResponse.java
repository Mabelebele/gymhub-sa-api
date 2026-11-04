package za.co.gymhubsa.api.trainer.dto;

public record TrainerPerformanceResponse(
        Long trainerId,
        String trainerFullName,
        Integer yearsOfExperience,
        long activeMemberCount,
        long totalAssignedEver
) {
}

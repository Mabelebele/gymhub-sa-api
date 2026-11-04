package za.co.gymhubsa.api.reporting.dto;

public record PlanDistributionEntry(
        Long planId,
        String planName,
        long activeMemberCount
) {
}

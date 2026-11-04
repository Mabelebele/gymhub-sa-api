package za.co.gymhubsa.api.membership.dto;

import za.co.gymhubsa.api.membership.entity.MembershipPlan;

import java.math.BigDecimal;

public record MembershipPlanResponse(
        Long id,
        String name,
        String description,
        Integer durationDays,
        BigDecimal price,
        boolean active
) {
    public static MembershipPlanResponse from(MembershipPlan plan) {
        return new MembershipPlanResponse(plan.getId(), plan.getName(), plan.getDescription(),
                plan.getDurationDays(), plan.getPrice(), plan.isActive());
    }
}

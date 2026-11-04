package za.co.gymhubsa.api.diet.dto;

import za.co.gymhubsa.api.diet.entity.GoalType;
import za.co.gymhubsa.api.diet.entity.NutritionGoal;
import za.co.gymhubsa.api.diet.entity.NutritionGoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NutritionGoalResponse(
        Long id,
        Long memberId,
        GoalType goalType,
        BigDecimal startWeightKg,
        BigDecimal targetWeightKg,
        LocalDate targetDate,
        NutritionGoalStatus status
) {
    public static NutritionGoalResponse from(NutritionGoal goal) {
        return new NutritionGoalResponse(goal.getId(), goal.getMember().getId(), goal.getGoalType(),
                goal.getStartWeightKg(), goal.getTargetWeightKg(), goal.getTargetDate(), goal.getStatus());
    }
}

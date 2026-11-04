package za.co.gymhubsa.api.diet.dto;

import jakarta.validation.constraints.NotNull;
import za.co.gymhubsa.api.diet.entity.GoalType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NutritionGoalRequest(
        @NotNull(message = "Goal type is required")
        GoalType goalType,

        BigDecimal startWeightKg,
        BigDecimal targetWeightKg,
        LocalDate targetDate
) {
}

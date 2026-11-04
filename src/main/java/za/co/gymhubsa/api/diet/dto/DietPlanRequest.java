package za.co.gymhubsa.api.diet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DietPlanRequest(
        @NotBlank(message = "Plan name is required")
        String name,

        String description,
        Integer dailyCalorieTarget,
        Integer proteinGrams,
        Integer carbsGrams,
        Integer fatGrams,

        @NotEmpty(message = "At least one meal is required")
        @Valid
        List<DietMealRequest> meals
) {
}

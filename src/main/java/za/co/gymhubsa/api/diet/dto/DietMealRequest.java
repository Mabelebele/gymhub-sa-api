package za.co.gymhubsa.api.diet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DietMealRequest(
        @NotBlank(message = "Meal name is required")
        String name,

        String description,
        Integer calories,

        @NotNull(message = "Sort order is required")
        Integer sortOrder
) {
}

package za.co.gymhubsa.api.diet.dto;

import za.co.gymhubsa.api.diet.entity.DietMeal;

public record DietMealResponse(
        Long id,
        String name,
        String description,
        Integer calories,
        Integer sortOrder
) {
    public static DietMealResponse from(DietMeal meal) {
        return new DietMealResponse(meal.getId(), meal.getName(), meal.getDescription(),
                meal.getCalories(), meal.getSortOrder());
    }
}

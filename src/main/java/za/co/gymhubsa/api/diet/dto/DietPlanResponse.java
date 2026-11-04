package za.co.gymhubsa.api.diet.dto;

import za.co.gymhubsa.api.diet.entity.DietPlan;

import java.util.List;

public record DietPlanResponse(
        Long id,
        String name,
        String description,
        Integer dailyCalorieTarget,
        Integer proteinGrams,
        Integer carbsGrams,
        Integer fatGrams,
        boolean active,
        List<DietMealResponse> meals
) {
    public static DietPlanResponse from(DietPlan plan) {
        List<DietMealResponse> meals = plan.getMeals().stream().map(DietMealResponse::from).toList();
        return new DietPlanResponse(plan.getId(), plan.getName(), plan.getDescription(), plan.getDailyCalorieTarget(),
                plan.getProteinGrams(), plan.getCarbsGrams(), plan.getFatGrams(), plan.isActive(), meals);
    }

    /** Lightweight variant without the meal list, for list/search views. */
    public static DietPlanResponse summary(DietPlan plan) {
        return new DietPlanResponse(plan.getId(), plan.getName(), plan.getDescription(), plan.getDailyCalorieTarget(),
                plan.getProteinGrams(), plan.getCarbsGrams(), plan.getFatGrams(), plan.isActive(), List.of());
    }
}

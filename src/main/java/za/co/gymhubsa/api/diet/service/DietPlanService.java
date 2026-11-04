package za.co.gymhubsa.api.diet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.diet.dto.DietMealRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanResponse;
import za.co.gymhubsa.api.diet.entity.DietMeal;
import za.co.gymhubsa.api.diet.entity.DietPlan;
import za.co.gymhubsa.api.diet.repository.DietPlanRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;

    @Transactional
    public DietPlanResponse create(DietPlanRequest request) {
        DietPlan plan = DietPlan.builder()
                .name(request.name())
                .description(request.description())
                .dailyCalorieTarget(request.dailyCalorieTarget())
                .proteinGrams(request.proteinGrams())
                .carbsGrams(request.carbsGrams())
                .fatGrams(request.fatGrams())
                .active(true)
                .meals(new ArrayList<>())
                .build();

        request.meals().forEach(mealRequest -> plan.getMeals().add(toMeal(plan, mealRequest)));

        dietPlanRepository.save(plan);
        return DietPlanResponse.from(plan);
    }

    @Transactional(readOnly = true)
    public List<DietPlanResponse> list(boolean includeInactive) {
        List<DietPlan> plans = includeInactive ? dietPlanRepository.findAll() : dietPlanRepository.findByActiveTrue();
        return plans.stream().map(DietPlanResponse::summary).toList();
    }

    @Transactional(readOnly = true)
    public DietPlanResponse getById(Long id) {
        return DietPlanResponse.from(findPlanOrThrow(id));
    }

    @Transactional
    public DietPlanResponse update(Long id, DietPlanRequest request) {
        DietPlan plan = findPlanOrThrow(id);

        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setDailyCalorieTarget(request.dailyCalorieTarget());
        plan.setProteinGrams(request.proteinGrams());
        plan.setCarbsGrams(request.carbsGrams());
        plan.setFatGrams(request.fatGrams());

        plan.getMeals().clear();
        request.meals().forEach(mealRequest -> plan.getMeals().add(toMeal(plan, mealRequest)));

        dietPlanRepository.save(plan);
        return DietPlanResponse.from(plan);
    }

    /** Soft-deletes a plan (marks inactive) so existing assignments keep a valid reference. */
    @Transactional
    public void deactivate(Long id) {
        DietPlan plan = findPlanOrThrow(id);
        plan.setActive(false);
        dietPlanRepository.save(plan);
    }

    private DietMeal toMeal(DietPlan plan, DietMealRequest request) {
        return DietMeal.builder()
                .dietPlan(plan)
                .name(request.name())
                .description(request.description())
                .calories(request.calories())
                .sortOrder(request.sortOrder())
                .build();
    }

    private DietPlan findPlanOrThrow(Long id) {
        return dietPlanRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Diet plan", id));
    }
}

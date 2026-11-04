package za.co.gymhubsa.api.diet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.diet.dto.DietMealRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanResponse;
import za.co.gymhubsa.api.diet.entity.DietPlan;
import za.co.gymhubsa.api.diet.repository.DietPlanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DietPlanServiceTest {

    @Mock
    private DietPlanRepository dietPlanRepository;

    private DietPlanService dietPlanService;

    @BeforeEach
    void setUp() {
        dietPlanService = new DietPlanService(dietPlanRepository);
    }

    @Test
    void create_withMeals_savesPlanWithMeals() {
        DietPlanRequest request = new DietPlanRequest("High-Protein Cutting", "desc", 1800, 160, 150, 50,
                List.of(new DietMealRequest("Breakfast", "Oats + eggs", 450, 1),
                        new DietMealRequest("Lunch", "Chicken + rice", 600, 2)));

        DietPlanResponse response = dietPlanService.create(request);

        assertThat(response.name()).isEqualTo("High-Protein Cutting");
        assertThat(response.meals()).hasSize(2);
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(dietPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dietPlanService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivate_setsActiveFalse() {
        DietPlan plan = DietPlan.builder().name("Plan").active(true).meals(new ArrayList<>()).build();
        plan.setId(1L);
        when(dietPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        dietPlanService.deactivate(1L);

        assertThat(plan.isActive()).isFalse();
    }
}

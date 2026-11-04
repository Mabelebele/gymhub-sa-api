package za.co.gymhubsa.api.workout.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.workout.dto.WorkoutExerciseRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanResponse;
import za.co.gymhubsa.api.workout.entity.DifficultyLevel;
import za.co.gymhubsa.api.workout.entity.WorkoutExercise;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;
import za.co.gymhubsa.api.workout.repository.WorkoutPlanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutPlanServiceTest {

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    private WorkoutPlanService workoutPlanService;

    @BeforeEach
    void setUp() {
        workoutPlanService = new WorkoutPlanService(workoutPlanRepository);
    }

    @Test
    void create_withExercises_savesPlanWithExercises() {
        WorkoutPlanRequest request = new WorkoutPlanRequest("Beginner Full Body", "desc", DifficultyLevel.BEGINNER, 4,
                List.of(new WorkoutExerciseRequest("Squat", 3, 10, 60, 1, null),
                        new WorkoutExerciseRequest("Push-up", 3, 12, 45, 2, null)));

        WorkoutPlanResponse response = workoutPlanService.create(request);

        assertThat(response.name()).isEqualTo("Beginner Full Body");
        assertThat(response.exercises()).hasSize(2);
        assertThat(response.exercises().get(0).name()).isEqualTo("Squat");
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(workoutPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutPlanService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_replacesExercises() {
        WorkoutPlan plan = WorkoutPlan.builder().name("Old Name").active(true).exercises(new ArrayList<>()).build();
        plan.setId(1L);
        plan.getExercises().add(WorkoutExercise.builder().workoutPlan(plan).name("Old Exercise")
                .sets(1).reps(1).sortOrder(1).build());
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        WorkoutPlanRequest request = new WorkoutPlanRequest("New Name", "desc", DifficultyLevel.INTERMEDIATE, 6,
                List.of(new WorkoutExerciseRequest("Deadlift", 4, 8, 90, 1, null)));

        WorkoutPlanResponse response = workoutPlanService.update(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.exercises()).hasSize(1);
        assertThat(response.exercises().get(0).name()).isEqualTo("Deadlift");
    }

    @Test
    void deactivate_setsActiveFalse() {
        WorkoutPlan plan = WorkoutPlan.builder().name("Plan").active(true).exercises(new ArrayList<>()).build();
        plan.setId(1L);
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        workoutPlanService.deactivate(1L);

        assertThat(plan.isActive()).isFalse();
    }
}

package za.co.gymhubsa.api.workout.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.workout.dto.WorkoutExerciseRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanResponse;
import za.co.gymhubsa.api.workout.entity.WorkoutExercise;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;
import za.co.gymhubsa.api.workout.repository.WorkoutPlanRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;

    @Transactional
    public WorkoutPlanResponse create(WorkoutPlanRequest request) {
        WorkoutPlan plan = WorkoutPlan.builder()
                .name(request.name())
                .description(request.description())
                .difficultyLevel(request.difficultyLevel())
                .durationWeeks(request.durationWeeks())
                .active(true)
                .exercises(new ArrayList<>())
                .build();

        request.exercises().forEach(exerciseRequest -> plan.getExercises().add(toExercise(plan, exerciseRequest)));

        workoutPlanRepository.save(plan);
        return WorkoutPlanResponse.from(plan);
    }

    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> list(boolean includeInactive) {
        List<WorkoutPlan> plans = includeInactive ? workoutPlanRepository.findAll() : workoutPlanRepository.findByActiveTrue();
        return plans.stream().map(WorkoutPlanResponse::summary).toList();
    }

    @Transactional(readOnly = true)
    public WorkoutPlanResponse getById(Long id) {
        return WorkoutPlanResponse.from(findPlanOrThrow(id));
    }

    @Transactional
    public WorkoutPlanResponse update(Long id, WorkoutPlanRequest request) {
        WorkoutPlan plan = findPlanOrThrow(id);

        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setDifficultyLevel(request.difficultyLevel());
        plan.setDurationWeeks(request.durationWeeks());

        plan.getExercises().clear();
        request.exercises().forEach(exerciseRequest -> plan.getExercises().add(toExercise(plan, exerciseRequest)));

        workoutPlanRepository.save(plan);
        return WorkoutPlanResponse.from(plan);
    }

    /** Soft-deletes a plan (marks inactive) so existing assignments keep a valid reference. */
    @Transactional
    public void deactivate(Long id) {
        WorkoutPlan plan = findPlanOrThrow(id);
        plan.setActive(false);
        workoutPlanRepository.save(plan);
    }

    private WorkoutExercise toExercise(WorkoutPlan plan, WorkoutExerciseRequest request) {
        return WorkoutExercise.builder()
                .workoutPlan(plan)
                .name(request.name())
                .sets(request.sets())
                .reps(request.reps())
                .restSeconds(request.restSeconds())
                .sortOrder(request.sortOrder())
                .notes(request.notes())
                .build();
    }

    private WorkoutPlan findPlanOrThrow(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Workout plan", id));
    }
}

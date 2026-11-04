package za.co.gymhubsa.api.workout.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;

import java.util.List;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findByActiveTrue();
}

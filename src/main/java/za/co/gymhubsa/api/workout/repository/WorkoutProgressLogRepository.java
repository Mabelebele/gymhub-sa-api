package za.co.gymhubsa.api.workout.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.workout.entity.WorkoutProgressLog;

public interface WorkoutProgressLogRepository extends JpaRepository<WorkoutProgressLog, Long> {

    Page<WorkoutProgressLog> findByMemberWorkoutPlanIdOrderByLogDateDesc(Long memberWorkoutPlanId, Pageable pageable);
}

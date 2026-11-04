package za.co.gymhubsa.api.workout.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.workout.entity.MemberWorkoutPlan;
import za.co.gymhubsa.api.workout.entity.WorkoutAssignmentStatus;

import java.util.Optional;

public interface MemberWorkoutPlanRepository extends JpaRepository<MemberWorkoutPlan, Long> {

    Optional<MemberWorkoutPlan> findFirstByMemberIdAndStatusOrderByAssignedDateDesc(Long memberId, WorkoutAssignmentStatus status);

    Page<MemberWorkoutPlan> findByMemberIdOrderByAssignedDateDesc(Long memberId, Pageable pageable);
}

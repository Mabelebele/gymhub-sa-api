package za.co.gymhubsa.api.diet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.diet.entity.NutritionGoal;
import za.co.gymhubsa.api.diet.entity.NutritionGoalStatus;

import java.util.Optional;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, Long> {

    Optional<NutritionGoal> findFirstByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, NutritionGoalStatus status);

    Page<NutritionGoal> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}

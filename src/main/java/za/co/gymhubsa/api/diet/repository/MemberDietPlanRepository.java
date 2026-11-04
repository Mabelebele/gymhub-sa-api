package za.co.gymhubsa.api.diet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.diet.entity.DietAssignmentStatus;
import za.co.gymhubsa.api.diet.entity.MemberDietPlan;

import java.util.Optional;

public interface MemberDietPlanRepository extends JpaRepository<MemberDietPlan, Long> {

    Optional<MemberDietPlan> findFirstByMemberIdAndStatusOrderByAssignedDateDesc(Long memberId, DietAssignmentStatus status);

    Page<MemberDietPlan> findByMemberIdOrderByAssignedDateDesc(Long memberId, Pageable pageable);
}

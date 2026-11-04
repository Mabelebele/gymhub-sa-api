package za.co.gymhubsa.api.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;

import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    List<MembershipPlan> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    Optional<MembershipPlan> findByNameIgnoreCase(String name);
}

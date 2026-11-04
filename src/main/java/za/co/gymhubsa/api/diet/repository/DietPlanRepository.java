package za.co.gymhubsa.api.diet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.diet.entity.DietPlan;

import java.util.List;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    List<DietPlan> findByActiveTrue();
}

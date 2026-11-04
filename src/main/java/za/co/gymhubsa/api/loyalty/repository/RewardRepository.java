package za.co.gymhubsa.api.loyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.loyalty.entity.Reward;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByActiveTrue();
}

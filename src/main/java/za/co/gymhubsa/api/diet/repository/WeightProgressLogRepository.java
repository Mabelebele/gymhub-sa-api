package za.co.gymhubsa.api.diet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.diet.entity.WeightProgressLog;

import java.util.Optional;

public interface WeightProgressLogRepository extends JpaRepository<WeightProgressLog, Long> {

    Page<WeightProgressLog> findByMemberIdOrderByLogDateDesc(Long memberId, Pageable pageable);

    Optional<WeightProgressLog> findFirstByMemberIdOrderByLogDateDesc(Long memberId);
}

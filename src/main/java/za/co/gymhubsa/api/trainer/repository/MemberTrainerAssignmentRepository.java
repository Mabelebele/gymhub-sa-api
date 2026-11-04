package za.co.gymhubsa.api.trainer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.trainer.entity.MemberTrainerAssignment;

import java.util.List;
import java.util.Optional;

public interface MemberTrainerAssignmentRepository extends JpaRepository<MemberTrainerAssignment, Long> {

    Optional<MemberTrainerAssignment> findFirstByMemberIdAndActiveTrue(Long memberId);

    List<MemberTrainerAssignment> findByTrainerIdAndActiveTrue(Long trainerId);

    long countByTrainerId(Long trainerId);

    long countByTrainerIdAndActiveTrue(Long trainerId);
}

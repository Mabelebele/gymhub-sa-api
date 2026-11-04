package za.co.gymhubsa.api.trainer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.trainer.dto.AssignTrainerRequest;
import za.co.gymhubsa.api.trainer.dto.MemberTrainerAssignmentResponse;
import za.co.gymhubsa.api.trainer.entity.MemberTrainerAssignment;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.repository.MemberTrainerAssignmentRepository;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Assigns/reassigns a trainer to a member. Reassigning ends the current
 * assignment (if any) and starts a new one, so the row history is the
 * assignment history.
 */
@Service
@RequiredArgsConstructor
public class TrainerAssignmentService {

    private final MemberTrainerAssignmentRepository assignmentRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    @Transactional
    public MemberTrainerAssignmentResponse assign(AssignTrainerRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> ResourceNotFoundException.of("Member", request.memberId()));
        Trainer trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Trainer", request.trainerId()));

        assignmentRepository.findFirstByMemberIdAndActiveTrue(member.getId()).ifPresent(current -> {
            current.setActive(false);
            current.setEndDate(LocalDate.now());
            assignmentRepository.save(current);
        });

        MemberTrainerAssignment assignment = MemberTrainerAssignment.builder()
                .member(member)
                .trainer(trainer)
                .assignedDate(LocalDate.now())
                .active(true)
                .build();
        assignmentRepository.save(assignment);

        return MemberTrainerAssignmentResponse.from(assignment);
    }

    @Transactional(readOnly = true)
    public MemberTrainerAssignmentResponse getCurrentForMember(Long memberId) {
        return assignmentRepository.findFirstByMemberIdAndActiveTrue(memberId)
                .map(MemberTrainerAssignmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Member has no assigned trainer"));
    }

    @Transactional(readOnly = true)
    public List<MemberTrainerAssignmentResponse> getMembersForTrainer(Long trainerId) {
        return assignmentRepository.findByTrainerIdAndActiveTrue(trainerId).stream()
                .map(MemberTrainerAssignmentResponse::from)
                .toList();
    }
}

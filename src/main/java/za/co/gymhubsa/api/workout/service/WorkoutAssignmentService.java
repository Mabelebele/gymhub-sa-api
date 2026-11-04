package za.co.gymhubsa.api.workout.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.workout.dto.AssignWorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.MemberWorkoutPlanResponse;
import za.co.gymhubsa.api.workout.entity.MemberWorkoutPlan;
import za.co.gymhubsa.api.workout.entity.WorkoutAssignmentStatus;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;
import za.co.gymhubsa.api.workout.repository.MemberWorkoutPlanRepository;
import za.co.gymhubsa.api.workout.repository.WorkoutPlanRepository;

import java.time.LocalDate;

/**
 * Assigns workout plans to members. Assigning a new plan automatically
 * cancels any currently active one for that member, so a member always has
 * at most one active plan, and prior assignments form their workout history.
 */
@Service
@RequiredArgsConstructor
public class WorkoutAssignmentService {

    private final MemberWorkoutPlanRepository memberWorkoutPlanRepository;
    private final MemberRepository memberRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    @Transactional
    public MemberWorkoutPlanResponse assign(AssignWorkoutPlanRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> ResourceNotFoundException.of("Member", request.memberId()));
        WorkoutPlan plan = workoutPlanRepository.findById(request.workoutPlanId())
                .orElseThrow(() -> ResourceNotFoundException.of("Workout plan", request.workoutPlanId()));

        if (!plan.isActive()) {
            throw new BadRequestException("Workout plan is no longer active");
        }

        memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(member.getId(), WorkoutAssignmentStatus.ACTIVE)
                .ifPresent(current -> {
                    current.setStatus(WorkoutAssignmentStatus.CANCELLED);
                    memberWorkoutPlanRepository.save(current);
                });

        MemberWorkoutPlan assignment = MemberWorkoutPlan.builder()
                .member(member)
                .workoutPlan(plan)
                .assignedDate(LocalDate.now())
                .status(WorkoutAssignmentStatus.ACTIVE)
                .build();
        memberWorkoutPlanRepository.save(assignment);

        return MemberWorkoutPlanResponse.from(assignment);
    }

    @Transactional
    public MemberWorkoutPlanResponse complete(Long memberWorkoutPlanId) {
        MemberWorkoutPlan assignment = findAssignmentOrThrow(memberWorkoutPlanId);
        assignment.setStatus(WorkoutAssignmentStatus.COMPLETED);
        memberWorkoutPlanRepository.save(assignment);
        return MemberWorkoutPlanResponse.from(assignment);
    }

    @Transactional(readOnly = true)
    public MemberWorkoutPlanResponse getCurrentForMember(Long memberId) {
        return memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(memberId, WorkoutAssignmentStatus.ACTIVE)
                .map(MemberWorkoutPlanResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Member has no active workout plan"));
    }

    @Transactional(readOnly = true)
    public Page<MemberWorkoutPlanResponse> getHistory(Long memberId, Pageable pageable) {
        return memberWorkoutPlanRepository.findByMemberIdOrderByAssignedDateDesc(memberId, pageable)
                .map(MemberWorkoutPlanResponse::from);
    }

    @Transactional(readOnly = true)
    public MemberWorkoutPlanResponse getMyCurrent(String email) {
        return getCurrentForMember(resolveMemberIdByEmail(email));
    }

    @Transactional(readOnly = true)
    public Page<MemberWorkoutPlanResponse> getMyHistory(String email, Pageable pageable) {
        return getHistory(resolveMemberIdByEmail(email), pageable);
    }

    private Long resolveMemberIdByEmail(String email) {
        return memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"))
                .getId();
    }

    private MemberWorkoutPlan findAssignmentOrThrow(Long id) {
        return memberWorkoutPlanRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Workout plan assignment", id));
    }
}

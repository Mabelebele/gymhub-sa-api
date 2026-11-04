package za.co.gymhubsa.api.diet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.diet.dto.AssignDietPlanRequest;
import za.co.gymhubsa.api.diet.dto.MemberDietPlanResponse;
import za.co.gymhubsa.api.diet.entity.DietAssignmentStatus;
import za.co.gymhubsa.api.diet.entity.DietPlan;
import za.co.gymhubsa.api.diet.entity.MemberDietPlan;
import za.co.gymhubsa.api.diet.repository.DietPlanRepository;
import za.co.gymhubsa.api.diet.repository.MemberDietPlanRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.LocalDate;

/**
 * Assigns diet plans to members. Assigning a new plan automatically cancels
 * any currently active one, so a member always has at most one active plan,
 * and prior assignments form their diet plan history.
 */
@Service
@RequiredArgsConstructor
public class DietAssignmentService {

    private final MemberDietPlanRepository memberDietPlanRepository;
    private final MemberRepository memberRepository;
    private final DietPlanRepository dietPlanRepository;

    @Transactional
    public MemberDietPlanResponse assign(AssignDietPlanRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> ResourceNotFoundException.of("Member", request.memberId()));
        DietPlan plan = dietPlanRepository.findById(request.dietPlanId())
                .orElseThrow(() -> ResourceNotFoundException.of("Diet plan", request.dietPlanId()));

        if (!plan.isActive()) {
            throw new BadRequestException("Diet plan is no longer active");
        }

        memberDietPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(member.getId(), DietAssignmentStatus.ACTIVE)
                .ifPresent(current -> {
                    current.setStatus(DietAssignmentStatus.CANCELLED);
                    memberDietPlanRepository.save(current);
                });

        MemberDietPlan assignment = MemberDietPlan.builder()
                .member(member)
                .dietPlan(plan)
                .assignedDate(LocalDate.now())
                .status(DietAssignmentStatus.ACTIVE)
                .build();
        memberDietPlanRepository.save(assignment);

        return MemberDietPlanResponse.from(assignment);
    }

    @Transactional(readOnly = true)
    public MemberDietPlanResponse getCurrentForMember(Long memberId) {
        return memberDietPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(memberId, DietAssignmentStatus.ACTIVE)
                .map(MemberDietPlanResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Member has no active diet plan"));
    }

    @Transactional(readOnly = true)
    public Page<MemberDietPlanResponse> getHistory(Long memberId, Pageable pageable) {
        return memberDietPlanRepository.findByMemberIdOrderByAssignedDateDesc(memberId, pageable)
                .map(MemberDietPlanResponse::from);
    }

    @Transactional(readOnly = true)
    public MemberDietPlanResponse getMyCurrent(String email) {
        return getCurrentForMember(resolveMemberIdByEmail(email));
    }

    @Transactional(readOnly = true)
    public Page<MemberDietPlanResponse> getMyHistory(String email, Pageable pageable) {
        return getHistory(resolveMemberIdByEmail(email), pageable);
    }

    private Long resolveMemberIdByEmail(String email) {
        return memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"))
                .getId();
    }
}

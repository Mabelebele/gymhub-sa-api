package za.co.gymhubsa.api.diet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.diet.dto.NutritionGoalRequest;
import za.co.gymhubsa.api.diet.dto.NutritionGoalResponse;
import za.co.gymhubsa.api.diet.entity.NutritionGoal;
import za.co.gymhubsa.api.diet.entity.NutritionGoalStatus;
import za.co.gymhubsa.api.diet.repository.NutritionGoalRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class NutritionGoalService {

    private final NutritionGoalRepository nutritionGoalRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public NutritionGoalResponse setGoal(Long memberId, NutritionGoalRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", memberId));

        nutritionGoalRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(memberId, NutritionGoalStatus.ACTIVE)
                .ifPresent(current -> {
                    current.setStatus(NutritionGoalStatus.CANCELLED);
                    nutritionGoalRepository.save(current);
                });

        NutritionGoal goal = NutritionGoal.builder()
                .member(member)
                .goalType(request.goalType())
                .startWeightKg(request.startWeightKg())
                .targetWeightKg(request.targetWeightKg())
                .targetDate(request.targetDate())
                .status(NutritionGoalStatus.ACTIVE)
                .build();
        nutritionGoalRepository.save(goal);

        return NutritionGoalResponse.from(goal);
    }

    @Transactional
    public NutritionGoalResponse markAchieved(Long goalId) {
        NutritionGoal goal = findGoalOrThrow(goalId);
        goal.setStatus(NutritionGoalStatus.ACHIEVED);
        nutritionGoalRepository.save(goal);
        return NutritionGoalResponse.from(goal);
    }

    @Transactional(readOnly = true)
    public NutritionGoalResponse getCurrentForMember(Long memberId) {
        return nutritionGoalRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(memberId, NutritionGoalStatus.ACTIVE)
                .map(NutritionGoalResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Member has no active nutrition goal"));
    }

    @Transactional(readOnly = true)
    public Page<NutritionGoalResponse> getHistory(Long memberId, Pageable pageable) {
        return nutritionGoalRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(NutritionGoalResponse::from);
    }

    @Transactional(readOnly = true)
    public NutritionGoalResponse getMyCurrent(String email) {
        return getCurrentForMember(resolveMemberIdByEmail(email));
    }

    @Transactional(readOnly = true)
    public Page<NutritionGoalResponse> getMyHistory(String email, Pageable pageable) {
        return getHistory(resolveMemberIdByEmail(email), pageable);
    }

    private Long resolveMemberIdByEmail(String email) {
        return memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"))
                .getId();
    }

    private NutritionGoal findGoalOrThrow(Long id) {
        return nutritionGoalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nutrition goal", id));
    }
}

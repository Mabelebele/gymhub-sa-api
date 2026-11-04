package za.co.gymhubsa.api.membership.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.membership.dto.MembershipPlanRequest;
import za.co.gymhubsa.api.membership.dto.MembershipPlanResponse;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;
import za.co.gymhubsa.api.membership.repository.MembershipPlanRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;

    @Transactional
    public MembershipPlanResponse create(MembershipPlanRequest request) {
        if (membershipPlanRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("A membership plan with this name already exists");
        }

        MembershipPlan plan = MembershipPlan.builder()
                .name(request.name())
                .description(request.description())
                .durationDays(request.durationDays())
                .price(request.price())
                .active(true)
                .build();
        membershipPlanRepository.save(plan);

        return MembershipPlanResponse.from(plan);
    }

    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> list(boolean includeInactive) {
        List<MembershipPlan> plans = includeInactive
                ? membershipPlanRepository.findAll()
                : membershipPlanRepository.findByActiveTrue();
        return plans.stream().map(MembershipPlanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MembershipPlanResponse getById(Long id) {
        return MembershipPlanResponse.from(findPlanOrThrow(id));
    }

    @Transactional
    public MembershipPlanResponse update(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = findPlanOrThrow(id);

        if (!plan.getName().equalsIgnoreCase(request.name())
                && membershipPlanRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("A membership plan with this name already exists");
        }

        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setDurationDays(request.durationDays());
        plan.setPrice(request.price());
        membershipPlanRepository.save(plan);

        return MembershipPlanResponse.from(plan);
    }

    /**
     * Soft-deletes a plan (marks inactive) rather than removing it, so
     * existing {@code MemberMembership} history keeps a valid reference.
     */
    @Transactional
    public void deactivate(Long id) {
        MembershipPlan plan = findPlanOrThrow(id);
        plan.setActive(false);
        membershipPlanRepository.save(plan);
    }

    private MembershipPlan findPlanOrThrow(Long id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Membership plan", id));
    }
}

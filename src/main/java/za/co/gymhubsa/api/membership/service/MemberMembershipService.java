package za.co.gymhubsa.api.membership.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.membership.dto.AssignMembershipRequest;
import za.co.gymhubsa.api.membership.dto.ChangePlanRequest;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.entity.MemberMembership;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.membership.repository.MembershipPlanRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Assigns, renews, upgrades/downgrades, and tracks expiry of a member's
 * membership. Every state change creates a new {@link MemberMembership} row
 * rather than mutating in place, so the row history for a member doubles as
 * their membership history.
 */
@Service
@RequiredArgsConstructor
public class MemberMembershipService {

    private final MemberMembershipRepository memberMembershipRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository membershipPlanRepository;

    @Transactional
    public MemberMembershipResponse assign(AssignMembershipRequest request) {
        Member member = findMemberOrThrow(request.memberId());

        if (memberMembershipRepository
                .findFirstByMemberIdAndStatusOrderByStartDateDesc(member.getId(), MembershipStatus.ACTIVE)
                .isPresent()) {
            throw new BadRequestException(
                    "This member already has an active membership. Use renew or change-plan instead.");
        }

        MembershipPlan plan = findActivePlanOrThrow(request.planId());
        MemberMembership membership = startNewMembership(member, plan, LocalDate.now());
        memberMembershipRepository.save(membership);

        return MemberMembershipResponse.from(membership);
    }

    @Transactional
    public MemberMembershipResponse renew(Long memberId) {
        Member member = findMemberOrThrow(memberId);
        MemberMembership current = findLatestOrThrow(memberId);
        MembershipPlan plan = findActivePlanOrThrow(current.getMembershipPlan().getId());

        if (current.getStatus() == MembershipStatus.ACTIVE) {
            current.setStatus(MembershipStatus.EXPIRED);
            memberMembershipRepository.save(current);
        }

        // Renewing before expiry extends from the current end date; renewing
        // after expiry starts a fresh period from today.
        LocalDate start = current.getEndDate().isAfter(LocalDate.now()) ? current.getEndDate() : LocalDate.now();
        MemberMembership renewed = startNewMembership(member, plan, start);
        memberMembershipRepository.save(renewed);

        return MemberMembershipResponse.from(renewed);
    }

    @Transactional
    public MemberMembershipResponse changePlan(Long memberId, ChangePlanRequest request) {
        Member member = findMemberOrThrow(memberId);
        MemberMembership current = memberMembershipRepository
                .findFirstByMemberIdAndStatusOrderByStartDateDesc(memberId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Member has no active membership to change"));

        MembershipPlan newPlan = findActivePlanOrThrow(request.newPlanId());

        current.setStatus(MembershipStatus.CANCELLED);
        memberMembershipRepository.save(current);

        MemberMembership updated = startNewMembership(member, newPlan, LocalDate.now());
        memberMembershipRepository.save(updated);

        return MemberMembershipResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public MemberMembershipResponse getCurrent(Long memberId) {
        return memberMembershipRepository
                .findFirstByMemberIdAndStatusOrderByStartDateDesc(memberId, MembershipStatus.ACTIVE)
                .map(MemberMembershipResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Member has no active membership"));
    }

    @Transactional(readOnly = true)
    public Page<MemberMembershipResponse> getHistory(Long memberId, Pageable pageable) {
        return memberMembershipRepository.findByMemberIdOrderByStartDateDesc(memberId, pageable)
                .map(MemberMembershipResponse::from);
    }

    @Transactional(readOnly = true)
    public List<MemberMembershipResponse> getExpiringSoon(int days) {
        LocalDate today = LocalDate.now();
        return memberMembershipRepository
                .findByStatusAndEndDateBetween(MembershipStatus.ACTIVE, today, today.plusDays(days))
                .stream()
                .map(MemberMembershipResponse::from)
                .toList();
    }

    /**
     * Flips any ACTIVE membership whose end date has passed to EXPIRED.
     * Invoked daily by {@code MembershipExpiryScheduler}.
     */
    @Transactional
    public int expireOverdueMemberships() {
        List<MemberMembership> overdue = memberMembershipRepository
                .findByStatusAndEndDateBefore(MembershipStatus.ACTIVE, LocalDate.now());
        overdue.forEach(m -> m.setStatus(MembershipStatus.EXPIRED));
        memberMembershipRepository.saveAll(overdue);
        return overdue.size();
    }

    private MemberMembership startNewMembership(Member member, MembershipPlan plan, LocalDate startDate) {
        return MemberMembership.builder()
                .member(member)
                .membershipPlan(plan)
                .startDate(startDate)
                .endDate(startDate.plusDays(plan.getDurationDays()))
                .pricePaid(plan.getPrice())
                .status(MembershipStatus.ACTIVE)
                .build();
    }

    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", memberId));
    }

    private MemberMembership findLatestOrThrow(Long memberId) {
        return memberMembershipRepository.findByMemberIdOrderByStartDateDesc(memberId, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new BadRequestException("Member has no prior membership to renew"));
    }

    private MembershipPlan findActivePlanOrThrow(Long planId) {
        MembershipPlan plan = membershipPlanRepository.findById(planId)
                .orElseThrow(() -> ResourceNotFoundException.of("Membership plan", planId));
        if (!plan.isActive()) {
            throw new BadRequestException("Membership plan is no longer active");
        }
        return plan;
    }
}

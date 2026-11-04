package za.co.gymhubsa.api.membership.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.membership.dto.AssignMembershipRequest;
import za.co.gymhubsa.api.membership.dto.ChangePlanRequest;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.entity.MemberMembership;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.membership.repository.MembershipPlanRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberMembershipServiceTest {

    @Mock
    private MemberMembershipRepository memberMembershipRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MembershipPlanRepository membershipPlanRepository;

    private MemberMembershipService service;

    private Member member;
    private MembershipPlan plan;

    @BeforeEach
    void setUp() {
        service = new MemberMembershipService(memberMembershipRepository, memberRepository, membershipPlanRepository);

        User user = User.builder().firstName("Sarah").lastName("Van Wyk").email("sarah@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);

        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);

        plan = MembershipPlan.builder().name("Monthly Basic").durationDays(30)
                .price(new BigDecimal("499.00")).active(true).build();
        plan.setId(10L);
    }

    @Test
    void assign_withNoExistingActiveMembership_createsOne() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findFirstByMemberIdAndStatusOrderByStartDateDesc(1L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(membershipPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        MemberMembershipResponse response = service.assign(new AssignMembershipRequest(1L, 10L));

        assertThat(response.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(response.endDate()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    void assign_whenAlreadyActive_throwsBadRequest() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        MemberMembership existing = existingMembership(MembershipStatus.ACTIVE, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(25));
        when(memberMembershipRepository.findFirstByMemberIdAndStatusOrderByStartDateDesc(1L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.assign(new AssignMembershipRequest(1L, 10L)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void renew_beforeExpiry_extendsFromCurrentEndDate() {
        LocalDate futureEnd = LocalDate.now().plusDays(10);
        MemberMembership current = existingMembership(MembershipStatus.ACTIVE, LocalDate.now().minusDays(20), futureEnd);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findByMemberIdOrderByStartDateDesc(eq(1L), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(current)));
        when(membershipPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        MemberMembershipResponse response = service.renew(1L);

        assertThat(response.startDate()).isEqualTo(futureEnd);
        assertThat(response.endDate()).isEqualTo(futureEnd.plusDays(30));
        assertThat(current.getStatus()).isEqualTo(MembershipStatus.EXPIRED);
    }

    @Test
    void renew_afterExpiry_startsFromToday() {
        LocalDate pastEnd = LocalDate.now().minusDays(5);
        MemberMembership current = existingMembership(MembershipStatus.EXPIRED, LocalDate.now().minusDays(35), pastEnd);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findByMemberIdOrderByStartDateDesc(eq(1L), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(current)));
        when(membershipPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        MemberMembershipResponse response = service.renew(1L);

        assertThat(response.startDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void renew_withNoPriorMembership_throwsBadRequest() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findByMemberIdOrderByStartDateDesc(eq(1L), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        assertThatThrownBy(() -> service.renew(1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void changePlan_cancelsCurrentAndStartsNewOne() {
        MemberMembership current = existingMembership(MembershipStatus.ACTIVE, LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(25));
        MembershipPlan newPlan = MembershipPlan.builder().name("Annual Premium").durationDays(365)
                .price(new BigDecimal("4999.00")).active(true).build();
        newPlan.setId(20L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findFirstByMemberIdAndStatusOrderByStartDateDesc(1L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(current));
        when(membershipPlanRepository.findById(20L)).thenReturn(Optional.of(newPlan));

        MemberMembershipResponse response = service.changePlan(1L, new ChangePlanRequest(20L));

        assertThat(current.getStatus()).isEqualTo(MembershipStatus.CANCELLED);
        assertThat(response.planId()).isEqualTo(20L);
    }

    @Test
    void expireOverdueMemberships_flipsActiveToExpired() {
        MemberMembership overdue = existingMembership(MembershipStatus.ACTIVE, LocalDate.now().minusDays(40),
                LocalDate.now().minusDays(1));
        when(memberMembershipRepository.findByStatusAndEndDateBefore(eq(MembershipStatus.ACTIVE), any()))
                .thenReturn(List.of(overdue));

        int count = service.expireOverdueMemberships();

        assertThat(count).isEqualTo(1);
        assertThat(overdue.getStatus()).isEqualTo(MembershipStatus.EXPIRED);
    }

    private MemberMembership existingMembership(MembershipStatus status, LocalDate start, LocalDate end) {
        MemberMembership membership = MemberMembership.builder()
                .member(member)
                .membershipPlan(plan)
                .startDate(start)
                .endDate(end)
                .pricePaid(plan.getPrice())
                .status(status)
                .build();
        membership.setId(100L);
        return membership;
    }
}

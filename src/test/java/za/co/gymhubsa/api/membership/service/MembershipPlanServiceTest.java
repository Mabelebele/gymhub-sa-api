package za.co.gymhubsa.api.membership.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.membership.dto.MembershipPlanRequest;
import za.co.gymhubsa.api.membership.dto.MembershipPlanResponse;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;
import za.co.gymhubsa.api.membership.repository.MembershipPlanRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipPlanServiceTest {

    @Mock
    private MembershipPlanRepository membershipPlanRepository;

    private MembershipPlanService membershipPlanService;

    @BeforeEach
    void setUp() {
        membershipPlanService = new MembershipPlanService(membershipPlanRepository);
    }

    @Test
    void create_withUniqueName_savesPlan() {
        MembershipPlanRequest request = new MembershipPlanRequest("Monthly Basic", "Gym access", 30,
                new BigDecimal("499.00"));
        when(membershipPlanRepository.existsByNameIgnoreCase("Monthly Basic")).thenReturn(false);

        MembershipPlanResponse response = membershipPlanService.create(request);

        assertThat(response.name()).isEqualTo("Monthly Basic");
        assertThat(response.active()).isTrue();
    }

    @Test
    void create_withDuplicateName_throwsBadRequest() {
        MembershipPlanRequest request = new MembershipPlanRequest("Monthly Basic", "Gym access", 30,
                new BigDecimal("499.00"));
        when(membershipPlanRepository.existsByNameIgnoreCase("Monthly Basic")).thenReturn(true);

        assertThatThrownBy(() -> membershipPlanService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(membershipPlanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipPlanService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivate_setsActiveFalse() {
        MembershipPlan plan = MembershipPlan.builder()
                .name("Annual Premium").durationDays(365).price(new BigDecimal("4999.00")).active(true).build();
        plan.setId(1L);
        when(membershipPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        membershipPlanService.deactivate(1L);

        assertThat(plan.isActive()).isFalse();
    }

    @Test
    void update_withNameClashOnAnotherPlan_throwsBadRequest() {
        MembershipPlan plan = MembershipPlan.builder()
                .name("Monthly Basic").durationDays(30).price(new BigDecimal("499.00")).active(true).build();
        plan.setId(1L);
        when(membershipPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(membershipPlanRepository.existsByNameIgnoreCase("Annual Premium")).thenReturn(true);

        MembershipPlanRequest request = new MembershipPlanRequest("Annual Premium", "desc", 365,
                new BigDecimal("4999.00"));

        assertThatThrownBy(() -> membershipPlanService.update(1L, request))
                .isInstanceOf(BadRequestException.class);
    }
}

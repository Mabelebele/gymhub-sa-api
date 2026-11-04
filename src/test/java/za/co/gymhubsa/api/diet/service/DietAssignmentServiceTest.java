package za.co.gymhubsa.api.diet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
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
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DietAssignmentServiceTest {

    @Mock
    private MemberDietPlanRepository memberDietPlanRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private DietPlanRepository dietPlanRepository;

    private DietAssignmentService service;

    private Member member;
    private DietPlan plan;

    @BeforeEach
    void setUp() {
        service = new DietAssignmentService(memberDietPlanRepository, memberRepository, dietPlanRepository);

        User user = User.builder().firstName("Ayanda").lastName("Cele").email("ayanda@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);

        plan = DietPlan.builder().name("Cutting Plan").active(true).build();
        plan.setId(10L);
    }

    @Test
    void assign_withNoExistingAssignment_createsActiveOne() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(dietPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(memberDietPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, DietAssignmentStatus.ACTIVE))
                .thenReturn(Optional.empty());

        MemberDietPlanResponse response = service.assign(new AssignDietPlanRequest(1L, 10L));

        assertThat(response.status()).isEqualTo(DietAssignmentStatus.ACTIVE);
    }

    @Test
    void assign_withExistingActiveAssignment_cancelsPrevious() {
        MemberDietPlan existing = MemberDietPlan.builder().member(member).dietPlan(plan)
                .assignedDate(LocalDate.now().minusDays(5)).status(DietAssignmentStatus.ACTIVE).build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(dietPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(memberDietPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, DietAssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        service.assign(new AssignDietPlanRequest(1L, 10L));

        assertThat(existing.getStatus()).isEqualTo(DietAssignmentStatus.CANCELLED);
    }

    @Test
    void assign_withInactivePlan_throwsBadRequest() {
        plan.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(dietPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service.assign(new AssignDietPlanRequest(1L, 10L)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getCurrentForMember_whenNoneActive_throwsNotFound() {
        when(memberDietPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, DietAssignmentStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentForMember(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

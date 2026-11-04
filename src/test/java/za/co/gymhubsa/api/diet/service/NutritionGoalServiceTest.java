package za.co.gymhubsa.api.diet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.diet.dto.NutritionGoalRequest;
import za.co.gymhubsa.api.diet.dto.NutritionGoalResponse;
import za.co.gymhubsa.api.diet.entity.GoalType;
import za.co.gymhubsa.api.diet.entity.NutritionGoal;
import za.co.gymhubsa.api.diet.entity.NutritionGoalStatus;
import za.co.gymhubsa.api.diet.repository.NutritionGoalRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionGoalServiceTest {

    @Mock
    private NutritionGoalRepository nutritionGoalRepository;
    @Mock
    private MemberRepository memberRepository;

    private NutritionGoalService service;

    private Member member;

    @BeforeEach
    void setUp() {
        service = new NutritionGoalService(nutritionGoalRepository, memberRepository);

        User user = User.builder().firstName("Reitumetse").lastName("Phiri").email("reitumetse@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);
    }

    @Test
    void setGoal_withNoExistingGoal_createsActiveOne() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(nutritionGoalRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(1L, NutritionGoalStatus.ACTIVE))
                .thenReturn(Optional.empty());

        NutritionGoalRequest request = new NutritionGoalRequest(GoalType.WEIGHT_LOSS,
                new BigDecimal("85.00"), new BigDecimal("78.00"), LocalDate.now().plusMonths(3));

        NutritionGoalResponse response = service.setGoal(1L, request);

        assertThat(response.status()).isEqualTo(NutritionGoalStatus.ACTIVE);
        assertThat(response.goalType()).isEqualTo(GoalType.WEIGHT_LOSS);
    }

    @Test
    void setGoal_withExistingActiveGoal_cancelsPrevious() {
        NutritionGoal existing = NutritionGoal.builder().member(member).goalType(GoalType.MAINTENANCE)
                .status(NutritionGoalStatus.ACTIVE).build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(nutritionGoalRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(1L, NutritionGoalStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        service.setGoal(1L, new NutritionGoalRequest(GoalType.WEIGHT_LOSS, null, new BigDecimal("75.00"), null));

        assertThat(existing.getStatus()).isEqualTo(NutritionGoalStatus.CANCELLED);
    }

    @Test
    void markAchieved_updatesStatus() {
        NutritionGoal goal = NutritionGoal.builder().member(member).goalType(GoalType.WEIGHT_LOSS)
                .status(NutritionGoalStatus.ACTIVE).build();
        goal.setId(5L);
        when(nutritionGoalRepository.findById(5L)).thenReturn(Optional.of(goal));

        NutritionGoalResponse response = service.markAchieved(5L);

        assertThat(response.status()).isEqualTo(NutritionGoalStatus.ACHIEVED);
    }

    @Test
    void getCurrentForMember_whenNoneActive_throwsNotFound() {
        when(nutritionGoalRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(1L, NutritionGoalStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentForMember(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

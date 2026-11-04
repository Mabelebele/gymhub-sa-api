package za.co.gymhubsa.api.workout.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.workout.dto.AssignWorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.MemberWorkoutPlanResponse;
import za.co.gymhubsa.api.workout.entity.MemberWorkoutPlan;
import za.co.gymhubsa.api.workout.entity.WorkoutAssignmentStatus;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;
import za.co.gymhubsa.api.workout.repository.MemberWorkoutPlanRepository;
import za.co.gymhubsa.api.workout.repository.WorkoutPlanRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutAssignmentServiceTest {

    @Mock
    private MemberWorkoutPlanRepository memberWorkoutPlanRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    private WorkoutAssignmentService service;

    private Member member;
    private WorkoutPlan plan;

    @BeforeEach
    void setUp() {
        service = new WorkoutAssignmentService(memberWorkoutPlanRepository, memberRepository, workoutPlanRepository);

        User user = User.builder().firstName("Thandi").lastName("Mahlangu").email("thandi@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);

        plan = WorkoutPlan.builder().name("Full Body").active(true).build();
        plan.setId(10L);
    }

    @Test
    void assign_withNoExistingAssignment_createsActiveOne() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(workoutPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, WorkoutAssignmentStatus.ACTIVE))
                .thenReturn(Optional.empty());

        MemberWorkoutPlanResponse response = service.assign(new AssignWorkoutPlanRequest(1L, 10L));

        assertThat(response.status()).isEqualTo(WorkoutAssignmentStatus.ACTIVE);
    }

    @Test
    void assign_withExistingActiveAssignment_cancelsPrevious() {
        MemberWorkoutPlan existing = MemberWorkoutPlan.builder().member(member).workoutPlan(plan)
                .assignedDate(LocalDate.now().minusDays(5)).status(WorkoutAssignmentStatus.ACTIVE).build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(workoutPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, WorkoutAssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        service.assign(new AssignWorkoutPlanRequest(1L, 10L));

        assertThat(existing.getStatus()).isEqualTo(WorkoutAssignmentStatus.CANCELLED);
    }

    @Test
    void assign_withInactivePlan_throwsBadRequest() {
        plan.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(workoutPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service.assign(new AssignWorkoutPlanRequest(1L, 10L)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getCurrentForMember_whenNoneActive_throwsNotFound() {
        when(memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, WorkoutAssignmentStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentForMember(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void complete_marksAssignmentCompleted() {
        MemberWorkoutPlan assignment = MemberWorkoutPlan.builder().member(member).workoutPlan(plan)
                .assignedDate(LocalDate.now()).status(WorkoutAssignmentStatus.ACTIVE).build();
        when(memberWorkoutPlanRepository.findById(5L)).thenReturn(Optional.of(assignment));

        MemberWorkoutPlanResponse response = service.complete(5L);

        assertThat(response.status()).isEqualTo(WorkoutAssignmentStatus.COMPLETED);
    }
}

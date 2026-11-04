package za.co.gymhubsa.api.workout.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogResponse;
import za.co.gymhubsa.api.workout.entity.MemberWorkoutPlan;
import za.co.gymhubsa.api.workout.entity.WorkoutAssignmentStatus;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;
import za.co.gymhubsa.api.workout.repository.MemberWorkoutPlanRepository;
import za.co.gymhubsa.api.workout.repository.WorkoutProgressLogRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutProgressServiceTest {

    @Mock
    private WorkoutProgressLogRepository workoutProgressLogRepository;
    @Mock
    private MemberWorkoutPlanRepository memberWorkoutPlanRepository;
    @Mock
    private MemberRepository memberRepository;

    private WorkoutProgressService service;

    private MemberWorkoutPlan assignment;

    @BeforeEach
    void setUp() {
        service = new WorkoutProgressService(workoutProgressLogRepository, memberWorkoutPlanRepository, memberRepository);

        User user = User.builder().firstName("Dineo").lastName("Motaung").email("dineo@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        Member member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);

        WorkoutPlan plan = WorkoutPlan.builder().name("Full Body").active(true).build();
        plan.setId(10L);

        assignment = MemberWorkoutPlan.builder().member(member).workoutPlan(plan)
                .assignedDate(LocalDate.now()).status(WorkoutAssignmentStatus.ACTIVE).build();
        assignment.setId(100L);
    }

    @Test
    void addLog_withoutExplicitDate_defaultsToToday() {
        when(memberWorkoutPlanRepository.findById(100L)).thenReturn(Optional.of(assignment));

        WorkoutProgressLogResponse response = service.addLog(100L,
                new WorkoutProgressLogRequest(null, 45, 3, "Felt good"));

        assertThat(response.logDate()).isEqualTo(LocalDate.now());
        assertThat(response.durationMinutes()).isEqualTo(45);
    }

    @Test
    void addLog_forUnknownAssignment_throwsNotFound() {
        when(memberWorkoutPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addLog(999L, new WorkoutProgressLogRequest(null, 30, 2, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addMyLog_resolvesActiveAssignmentFromEmail() {
        Member member = assignment.getMember();
        when(memberRepository.findByUser_EmailIgnoreCase("dineo@example.com")).thenReturn(Optional.of(member));
        when(memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, WorkoutAssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(assignment));
        when(memberWorkoutPlanRepository.findById(100L)).thenReturn(Optional.of(assignment));

        WorkoutProgressLogResponse response = service.addMyLog("dineo@example.com",
                new WorkoutProgressLogRequest(LocalDate.now(), 30, 4, "Good session"));

        assertThat(response.memberWorkoutPlanId()).isEqualTo(100L);
    }

    @Test
    void addMyLog_withNoActivePlan_throwsNotFound() {
        Member member = assignment.getMember();
        when(memberRepository.findByUser_EmailIgnoreCase("dineo@example.com")).thenReturn(Optional.of(member));
        when(memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(1L, WorkoutAssignmentStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMyLog("dineo@example.com",
                new WorkoutProgressLogRequest(null, 30, 3, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

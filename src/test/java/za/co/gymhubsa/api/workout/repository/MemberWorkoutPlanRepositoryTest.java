package za.co.gymhubsa.api.workout.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.workout.entity.MemberWorkoutPlan;
import za.co.gymhubsa.api.workout.entity.WorkoutAssignmentStatus;
import za.co.gymhubsa.api.workout.entity.WorkoutPlan;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberWorkoutPlanRepositoryTest {

    @Autowired
    private MemberWorkoutPlanRepository memberWorkoutPlanRepository;
    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findFirstByMemberIdAndStatus_andHistory_work() {
        User user = userRepository.save(User.builder()
                .firstName("Mpho").lastName("Letsoalo").email("mpho@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build());
        Member member = memberRepository.save(Member.builder()
                .user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build());
        WorkoutPlan plan = workoutPlanRepository.save(WorkoutPlan.builder().name("Full Body").active(true).build());

        MemberWorkoutPlan assignment = memberWorkoutPlanRepository.save(MemberWorkoutPlan.builder()
                .member(member).workoutPlan(plan).assignedDate(LocalDate.now())
                .status(WorkoutAssignmentStatus.ACTIVE).build());

        assertThat(memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(member.getId(), WorkoutAssignmentStatus.ACTIVE))
                .contains(assignment);

        assertThat(memberWorkoutPlanRepository.findByMemberIdOrderByAssignedDateDesc(member.getId(), PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }
}

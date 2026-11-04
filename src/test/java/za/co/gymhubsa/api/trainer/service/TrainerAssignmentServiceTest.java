package za.co.gymhubsa.api.trainer.service;

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
import za.co.gymhubsa.api.trainer.dto.AssignTrainerRequest;
import za.co.gymhubsa.api.trainer.dto.MemberTrainerAssignmentResponse;
import za.co.gymhubsa.api.trainer.entity.MemberTrainerAssignment;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;
import za.co.gymhubsa.api.trainer.repository.MemberTrainerAssignmentRepository;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerAssignmentServiceTest {

    @Mock
    private MemberTrainerAssignmentRepository assignmentRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TrainerRepository trainerRepository;

    private TrainerAssignmentService service;

    private Member member;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        service = new TrainerAssignmentService(assignmentRepository, memberRepository, trainerRepository);

        User memberUser = User.builder().firstName("Katlego").lastName("Molefe").email("katlego@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        memberUser.setId(1L);
        member = Member.builder().user(memberUser).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);

        User trainerUser = User.builder().firstName("Lindiwe").lastName("Dube").email("lindiwe@example.com")
                .password("hashed").role(Role.TRAINER).enabled(true).build();
        trainerUser.setId(2L);
        trainer = Trainer.builder().user(trainerUser).status(TrainerStatus.ACTIVE).hireDate(LocalDate.now()).build();
        trainer.setId(10L);
    }

    @Test
    void assign_withNoExistingAssignment_createsOne() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(trainerRepository.findById(10L)).thenReturn(Optional.of(trainer));
        when(assignmentRepository.findFirstByMemberIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        MemberTrainerAssignmentResponse response = service.assign(new AssignTrainerRequest(1L, 10L));

        assertThat(response.active()).isTrue();
        assertThat(response.trainerId()).isEqualTo(10L);
    }

    @Test
    void assign_withExistingAssignment_endsPreviousOne() {
        MemberTrainerAssignment existing = MemberTrainerAssignment.builder()
                .member(member).trainer(trainer).assignedDate(LocalDate.now().minusDays(10)).active(true).build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(trainerRepository.findById(10L)).thenReturn(Optional.of(trainer));
        when(assignmentRepository.findFirstByMemberIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));

        service.assign(new AssignTrainerRequest(1L, 10L));

        assertThat(existing.isActive()).isFalse();
        assertThat(existing.getEndDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void getCurrentForMember_whenNoneAssigned_throwsNotFound() {
        when(assignmentRepository.findFirstByMemberIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentForMember(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMembersForTrainer_returnsActiveAssignments() {
        MemberTrainerAssignment assignment = MemberTrainerAssignment.builder()
                .member(member).trainer(trainer).assignedDate(LocalDate.now()).active(true).build();
        when(assignmentRepository.findByTrainerIdAndActiveTrue(10L)).thenReturn(List.of(assignment));

        List<MemberTrainerAssignmentResponse> members = service.getMembersForTrainer(10L);

        assertThat(members).hasSize(1);
    }
}

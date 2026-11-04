package za.co.gymhubsa.api.trainer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.common.storage.FileStorageService;
import za.co.gymhubsa.api.trainer.dto.TrainerPerformanceResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerRegistrationRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerStatusUpdateRequest;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;
import za.co.gymhubsa.api.trainer.repository.MemberTrainerAssignmentRepository;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private MemberTrainerAssignmentRepository memberTrainerAssignmentRepository;

    private TrainerService trainerService;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(trainerRepository, userRepository, passwordEncoder, fileStorageService,
                memberTrainerAssignmentRepository);

        User user = User.builder().firstName("Lindiwe").lastName("Dube").email("lindiwe@example.com")
                .password("hashed").role(Role.TRAINER).enabled(true).build();
        user.setId(1L);

        trainer = Trainer.builder().user(user).specialization("Strength").yearsOfExperience(5)
                .status(TrainerStatus.ACTIVE).hireDate(LocalDate.now()).build();
        trainer.setId(1L);
    }

    @Test
    void register_withNewEmail_createsUserAndTrainer() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Lindiwe", "Dube",
                "lindiwe@example.com", "Password123!", "0821234567", "Strength", "Bio", 5);
        when(userRepository.existsByEmailIgnoreCase("lindiwe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");

        TrainerResponse response = trainerService.register(request);

        assertThat(response.firstName()).isEqualTo("Lindiwe");
        assertThat(response.status()).isEqualTo(TrainerStatus.ACTIVE);
        verify(userRepository).save(any(User.class));
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void register_withExistingEmail_throwsBadRequest() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Lindiwe", "Dube",
                "lindiwe@example.com", "Password123!", null, null, null, null);
        when(userRepository.existsByEmailIgnoreCase("lindiwe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> trainerService.register(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_changesStatus() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        TrainerResponse response = trainerService.updateStatus(1L, new TrainerStatusUpdateRequest(TrainerStatus.INACTIVE));

        assertThat(response.status()).isEqualTo(TrainerStatus.INACTIVE);
    }

    @Test
    void getPerformance_returnsAssignmentCounts() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(memberTrainerAssignmentRepository.countByTrainerIdAndActiveTrue(1L)).thenReturn(3L);
        when(memberTrainerAssignmentRepository.countByTrainerId(1L)).thenReturn(10L);

        TrainerPerformanceResponse response = trainerService.getPerformance(1L);

        assertThat(response.activeMemberCount()).isEqualTo(3L);
        assertThat(response.totalAssignedEver()).isEqualTo(10L);
    }
}

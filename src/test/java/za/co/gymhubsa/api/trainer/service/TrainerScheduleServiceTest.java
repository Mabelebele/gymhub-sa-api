package za.co.gymhubsa.api.trainer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.trainer.dto.TrainerScheduleRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerScheduleResponse;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerSchedule;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;
import za.co.gymhubsa.api.trainer.repository.TrainerScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerScheduleServiceTest {

    @Mock
    private TrainerScheduleRepository trainerScheduleRepository;
    @Mock
    private TrainerRepository trainerRepository;

    private TrainerScheduleService service;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        service = new TrainerScheduleService(trainerScheduleRepository, trainerRepository);

        User user = User.builder().firstName("Lindiwe").lastName("Dube").email("lindiwe@example.com")
                .password("hashed").role(Role.TRAINER).enabled(true).build();
        user.setId(1L);
        trainer = Trainer.builder().user(user).status(TrainerStatus.ACTIVE).hireDate(LocalDate.now()).build();
        trainer.setId(10L);
    }

    @Test
    void addSlot_withValidTimes_createsSlot() {
        when(trainerRepository.findById(10L)).thenReturn(Optional.of(trainer));
        TrainerScheduleRequest request = new TrainerScheduleRequest(DayOfWeek.MONDAY,
                LocalTime.of(6, 0), LocalTime.of(10, 0));

        TrainerScheduleResponse response = service.addSlot(10L, request);

        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void addSlot_withEndBeforeStart_throwsBadRequest() {
        TrainerScheduleRequest request = new TrainerScheduleRequest(DayOfWeek.MONDAY,
                LocalTime.of(10, 0), LocalTime.of(6, 0));

        assertThatThrownBy(() -> service.addSlot(10L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void removeSlot_belongingToAnotherTrainer_throwsBadRequest() {
        TrainerSchedule slot = TrainerSchedule.builder().trainer(trainer)
                .dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(6, 0)).endTime(LocalTime.of(10, 0)).build();
        when(trainerScheduleRepository.findById(1L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> service.removeSlot(999L, 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void removeSlot_whenMissing_throwsNotFound() {
        when(trainerScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeSlot(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

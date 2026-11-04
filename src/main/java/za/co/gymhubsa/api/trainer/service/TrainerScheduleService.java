package za.co.gymhubsa.api.trainer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.trainer.dto.TrainerScheduleRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerScheduleResponse;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerSchedule;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;
import za.co.gymhubsa.api.trainer.repository.TrainerScheduleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerScheduleService {

    private final TrainerScheduleRepository trainerScheduleRepository;
    private final TrainerRepository trainerRepository;

    @Transactional
    public TrainerScheduleResponse addSlot(Long trainerId, TrainerScheduleRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Trainer", trainerId));

        TrainerSchedule slot = TrainerSchedule.builder()
                .trainer(trainer)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
        trainerScheduleRepository.save(slot);

        return TrainerScheduleResponse.from(slot);
    }

    @Transactional(readOnly = true)
    public List<TrainerScheduleResponse> getSchedule(Long trainerId) {
        return trainerScheduleRepository.findByTrainerIdOrderByDayOfWeekAscStartTimeAsc(trainerId).stream()
                .map(TrainerScheduleResponse::from)
                .toList();
    }

    @Transactional
    public void removeSlot(Long trainerId, Long scheduleId) {
        TrainerSchedule slot = trainerScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Schedule slot", scheduleId));

        if (!slot.getTrainer().getId().equals(trainerId)) {
            throw new BadRequestException("Schedule slot does not belong to this trainer");
        }

        trainerScheduleRepository.delete(slot);
    }
}

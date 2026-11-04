package za.co.gymhubsa.api.trainer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.trainer.entity.TrainerSchedule;

import java.util.List;

public interface TrainerScheduleRepository extends JpaRepository<TrainerSchedule, Long> {

    List<TrainerSchedule> findByTrainerIdOrderByDayOfWeekAscStartTimeAsc(Long trainerId);
}

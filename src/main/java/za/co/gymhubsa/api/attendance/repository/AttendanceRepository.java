package za.co.gymhubsa.api.attendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.attendance.entity.Attendance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(Long memberId);

    Page<Attendance> findByMemberIdOrderByCheckInTimeDesc(Long memberId, Pageable pageable);

    List<Attendance> findByCheckInTimeBetweenOrderByCheckInTimeAsc(Instant from, Instant to);
}

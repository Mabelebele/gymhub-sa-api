package za.co.gymhubsa.api.attendance.dto;

import za.co.gymhubsa.api.attendance.entity.Attendance;
import za.co.gymhubsa.api.attendance.entity.CheckInMethod;

import java.time.Duration;
import java.time.Instant;

public record AttendanceResponse(
        Long id,
        Long memberId,
        String memberFullName,
        Instant checkInTime,
        Instant checkOutTime,
        CheckInMethod method,
        Long durationMinutes
) {
    public static AttendanceResponse from(Attendance attendance) {
        Long durationMinutes = attendance.getCheckOutTime() != null
                ? Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes()
                : null;

        return new AttendanceResponse(
                attendance.getId(),
                attendance.getMember().getId(),
                attendance.getMember().getUser().getFirstName() + " " + attendance.getMember().getUser().getLastName(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getMethod(),
                durationMinutes
        );
    }
}

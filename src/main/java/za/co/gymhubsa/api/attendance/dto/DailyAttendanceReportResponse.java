package za.co.gymhubsa.api.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyAttendanceReportResponse(
        LocalDate date,
        long totalCheckIns,
        List<AttendanceResponse> records
) {
}

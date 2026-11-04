package za.co.gymhubsa.api.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.attendance.dto.AttendanceResponse;
import za.co.gymhubsa.api.attendance.dto.CheckInRequest;
import za.co.gymhubsa.api.attendance.dto.CheckOutRequest;
import za.co.gymhubsa.api.attendance.dto.DailyAttendanceReportResponse;
import za.co.gymhubsa.api.attendance.dto.QrCheckInRequest;
import za.co.gymhubsa.api.attendance.entity.CheckInMethod;
import za.co.gymhubsa.api.attendance.service.AttendanceService;
import za.co.gymhubsa.api.common.dto.ApiResponse;

import java.time.LocalDate;

@Tag(name = "Attendance", description = "Member check-in/out, QR code check-in, history and daily reports")
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Manually check in a member (front desk)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(@Valid @RequestBody CheckInRequest request) {
        AttendanceResponse response = attendanceService.checkIn(request.memberId(), CheckInMethod.MANUAL);
        return ResponseEntity.ok(ApiResponse.success("Checked in successfully", response));
    }

    @Operation(summary = "Check in a member by scanning their QR membership card")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/check-in/qr")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkInByQr(@Valid @RequestBody QrCheckInRequest request) {
        AttendanceResponse response = attendanceService.checkInByQrCode(request.qrCode());
        return ResponseEntity.ok(ApiResponse.success("Checked in successfully", response));
    }

    @Operation(summary = "Check out a member (closes their open check-in)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(@Valid @RequestBody CheckOutRequest request) {
        AttendanceResponse response = attendanceService.checkOut(request.memberId());
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully", response));
    }

    @Operation(summary = "Get a member's attendance history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get the signed-in member's own attendance history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyHistory(authentication.getName(), pageable)));
    }

    @Operation(summary = "Get the daily attendance report (defaults to today)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/daily-report")
    public ResponseEntity<ApiResponse<DailyAttendanceReportResponse>> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate reportDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getDailyReport(reportDate)));
    }
}

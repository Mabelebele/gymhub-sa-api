package za.co.gymhubsa.api.reporting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.attendance.dto.DailyAttendanceReportResponse;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.payment.dto.RevenueReportResponse;
import za.co.gymhubsa.api.reporting.dto.LoyaltyReportResponse;
import za.co.gymhubsa.api.reporting.dto.MemberReportResponse;
import za.co.gymhubsa.api.reporting.dto.MembershipReportResponse;
import za.co.gymhubsa.api.reporting.dto.PaymentReportResponse;
import za.co.gymhubsa.api.reporting.dto.TrainerReportResponse;
import za.co.gymhubsa.api.reporting.service.ReportingService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reports", description = "Member, membership, attendance, payment, revenue, trainer, expiry and loyalty reports")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportingController {

    private final ReportingService reportingService;

    @Operation(summary = "Member report: status breakdown plus a searchable/paginated member list")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<MemberReportResponse>> getMemberReport(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MemberStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getMemberReport(keyword, status, pageable)));
    }

    @Operation(summary = "Membership report: status breakdown plus active-member distribution per plan")
    @GetMapping("/memberships")
    public ResponseEntity<ApiResponse<MembershipReportResponse>> getMembershipReport() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getMembershipReport()));
    }

    @Operation(summary = "Attendance report for a given date (defaults to today)")
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<DailyAttendanceReportResponse>> getAttendanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getAttendanceReport(date)));
    }

    @Operation(summary = "Payment report for a date range: totals plus breakdown by payment method")
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentReportResponse>> getPaymentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getPaymentReport(from, to)));
    }

    @Operation(summary = "Revenue report for a date range (defaults to current month)")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.withDayOfMonth(1);
        return ResponseEntity.ok(ApiResponse.success(reportingService.getRevenueReport(effectiveFrom, effectiveTo)));
    }

    @Operation(summary = "Trainer report: performance summary for every active trainer")
    @GetMapping("/trainers")
    public ResponseEntity<ApiResponse<TrainerReportResponse>> getTrainerReport() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getTrainerReport()));
    }

    @Operation(summary = "Membership expiry report: active memberships expiring within N days (default 30)")
    @GetMapping("/membership-expiry")
    public ResponseEntity<ApiResponse<List<MemberMembershipResponse>>> getMembershipExpiryReport(
            @RequestParam(required = false) Integer days) {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getMembershipExpiryReport(days)));
    }

    @Operation(summary = "Loyalty report: total points earned/redeemed and top point earners")
    @GetMapping("/loyalty")
    public ResponseEntity<ApiResponse<LoyaltyReportResponse>> getLoyaltyReport() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getLoyaltyReport()));
    }
}

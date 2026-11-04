package za.co.gymhubsa.api.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.attendance.repository.AttendanceRepository;
import za.co.gymhubsa.api.dashboard.dto.DashboardSummaryResponse;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.notification.dto.NotificationResponse;
import za.co.gymhubsa.api.notification.repository.NotificationRepository;
import za.co.gymhubsa.api.payment.dto.PaymentResponse;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;
import za.co.gymhubsa.api.payment.repository.PaymentRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Aggregates read-only metrics from across the existing modules (Member,
 * Membership, Attendance, Payment, Notification) into a single admin
 * dashboard view. Owns no data of its own.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** Gym operates in South Africa; "today"/"this month" boundaries use this zone regardless of server clock. */
    private static final ZoneId GYM_ZONE = ZoneId.of("Africa/Johannesburg");
    private static final int EXPIRING_SOON_WINDOW_DAYS = 7;

    private final MemberRepository memberRepository;
    private final MemberMembershipRepository memberMembershipRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        long totalMembers = memberRepository.countByStatus(MemberStatus.ACTIVE);
        long activeMemberships = memberMembershipRepository.countByStatus(MembershipStatus.ACTIVE);

        LocalDate today = LocalDate.now(GYM_ZONE);
        Instant startOfToday = today.atStartOfDay(GYM_ZONE).toInstant();
        Instant startOfTomorrow = today.plusDays(1).atStartOfDay(GYM_ZONE).toInstant();
        long todaysAttendanceCount = attendanceRepository.countByCheckInTimeBetween(startOfToday, startOfTomorrow);

        Instant startOfMonth = today.withDayOfMonth(1).atStartOfDay(GYM_ZONE).toInstant();
        var monthlyRevenue = paymentRepository.sumAmountByStatusAndPaidAtBetween(
                PaymentStatus.COMPLETED, startOfMonth, startOfTomorrow);
        long monthlyPaymentCount = paymentRepository.countByStatusAndPaidAtBetween(
                PaymentStatus.COMPLETED, startOfMonth, startOfTomorrow);

        var expiringSoon = memberMembershipRepository
                .findByStatusAndEndDateBetween(MembershipStatus.ACTIVE, today, today.plusDays(EXPIRING_SOON_WINDOW_DAYS))
                .stream().map(MemberMembershipResponse::from).toList();

        var recentPayments = paymentRepository.findTop5ByStatusOrderByPaidAtDesc(PaymentStatus.COMPLETED)
                .stream().map(PaymentResponse::from).toList();

        var recentNotifications = notificationRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(NotificationResponse::from).toList();

        return new DashboardSummaryResponse(totalMembers, activeMemberships, todaysAttendanceCount, monthlyRevenue,
                monthlyPaymentCount, expiringSoon, recentPayments, recentNotifications);
    }
}

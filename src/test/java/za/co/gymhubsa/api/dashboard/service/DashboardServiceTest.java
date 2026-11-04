package za.co.gymhubsa.api.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.attendance.repository.AttendanceRepository;
import za.co.gymhubsa.api.dashboard.dto.DashboardSummaryResponse;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.notification.repository.NotificationRepository;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;
import za.co.gymhubsa.api.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberMembershipRepository memberMembershipRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationRepository notificationRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(memberRepository, memberMembershipRepository,
                attendanceRepository, paymentRepository, notificationRepository);
    }

    @Test
    void getSummary_aggregatesAllMetrics() {
        when(memberRepository.countByStatus(MemberStatus.ACTIVE)).thenReturn(42L);
        when(memberMembershipRepository.countByStatus(MembershipStatus.ACTIVE)).thenReturn(35L);
        when(attendanceRepository.countByCheckInTimeBetween(any(), any())).thenReturn(12L);
        when(paymentRepository.sumAmountByStatusAndPaidAtBetween(eq(PaymentStatus.COMPLETED), any(), any()))
                .thenReturn(new BigDecimal("15000.00"));
        when(paymentRepository.countByStatusAndPaidAtBetween(eq(PaymentStatus.COMPLETED), any(), any()))
                .thenReturn(30L);
        when(memberMembershipRepository.findByStatusAndEndDateBetween(eq(MembershipStatus.ACTIVE), any(), any()))
                .thenReturn(List.of());
        when(paymentRepository.findTop5ByStatusOrderByPaidAtDesc(PaymentStatus.COMPLETED)).thenReturn(List.of());
        when(notificationRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of());

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.totalMembers()).isEqualTo(42L);
        assertThat(summary.activeMemberships()).isEqualTo(35L);
        assertThat(summary.todaysAttendanceCount()).isEqualTo(12L);
        assertThat(summary.monthlyRevenue()).isEqualByComparingTo("15000.00");
        assertThat(summary.monthlyPaymentCount()).isEqualTo(30L);
        assertThat(summary.membershipsExpiringSoon()).isEmpty();
        assertThat(summary.recentPayments()).isEmpty();
        assertThat(summary.recentNotifications()).isEmpty();
    }
}

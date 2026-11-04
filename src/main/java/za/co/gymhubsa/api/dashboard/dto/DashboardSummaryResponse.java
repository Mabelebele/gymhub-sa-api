package za.co.gymhubsa.api.dashboard.dto;

import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.notification.dto.NotificationResponse;
import za.co.gymhubsa.api.payment.dto.PaymentResponse;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long totalMembers,
        long activeMemberships,
        long todaysAttendanceCount,
        BigDecimal monthlyRevenue,
        long monthlyPaymentCount,
        List<MemberMembershipResponse> membershipsExpiringSoon,
        List<PaymentResponse> recentPayments,
        List<NotificationResponse> recentNotifications
) {
}

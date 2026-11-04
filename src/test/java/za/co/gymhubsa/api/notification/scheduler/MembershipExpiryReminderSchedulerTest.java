package za.co.gymhubsa.api.notification.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationType;
import za.co.gymhubsa.api.notification.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipExpiryReminderSchedulerTest {

    @Mock
    private MemberMembershipService memberMembershipService;
    @Mock
    private NotificationService notificationService;

    @Test
    void sendExpiryReminders_notifiesEachExpiringMember() {
        MembershipExpiryReminderScheduler scheduler =
                new MembershipExpiryReminderScheduler(memberMembershipService, notificationService);

        MemberMembershipResponse expiring = new MemberMembershipResponse(1L, 10L, "Jane Doe", 20L, "Monthly Basic",
                LocalDate.now().minusDays(27), LocalDate.now().plusDays(2), new BigDecimal("499.00"), MembershipStatus.ACTIVE);
        when(memberMembershipService.getExpiringSoon(3)).thenReturn(List.of(expiring));

        scheduler.sendExpiryReminders();

        verify(notificationService).sendToMember(eq(10L), eq(NotificationChannel.EMAIL),
                eq(NotificationType.MEMBERSHIP_EXPIRY), any(), any());
    }
}

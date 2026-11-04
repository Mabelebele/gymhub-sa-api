package za.co.gymhubsa.api.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationType;
import za.co.gymhubsa.api.notification.service.NotificationService;

/**
 * Daily job that emails members whose active membership expires within the
 * next 3 days. Runs after {@code MembershipExpiryScheduler} so same-day
 * expiries have already been settled before reminders are considered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipExpiryReminderScheduler {

    private static final int REMINDER_WINDOW_DAYS = 3;

    private final MemberMembershipService memberMembershipService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpiryReminders() {
        var expiringSoon = memberMembershipService.getExpiringSoon(REMINDER_WINDOW_DAYS);

        for (MemberMembershipResponse membership : expiringSoon) {
            String message = "Hi " + membership.memberFullName() + ", your " + membership.planName()
                    + " membership expires on " + membership.endDate() + ". Renew soon to keep your access active.";
            notificationService.sendToMember(membership.memberId(), NotificationChannel.EMAIL,
                    NotificationType.MEMBERSHIP_EXPIRY, "Your membership is expiring soon", message);
        }

        if (!expiringSoon.isEmpty()) {
            log.info("Sent {} membership expiry reminder(s)", expiringSoon.size());
        }
    }
}

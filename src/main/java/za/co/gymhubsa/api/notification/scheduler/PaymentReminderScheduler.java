package za.co.gymhubsa.api.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationType;
import za.co.gymhubsa.api.notification.service.NotificationService;
import za.co.gymhubsa.api.payment.dto.OutstandingBalanceResponse;
import za.co.gymhubsa.api.payment.service.PaymentService;

/** Daily job that emails members who currently have an outstanding balance. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReminderScheduler {

    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 30 8 * * *")
    public void sendPaymentReminders() {
        var outstanding = paymentService.getOutstandingBalances();

        for (OutstandingBalanceResponse balance : outstanding) {
            String message = "Hi " + balance.memberFullName() + ", you have an outstanding balance of R"
                    + balance.outstandingAmount() + " on your membership. Please settle it at your earliest convenience.";
            notificationService.sendToMember(balance.memberId(), NotificationChannel.EMAIL,
                    NotificationType.PAYMENT_REMINDER, "Outstanding balance reminder", message);
        }

        if (!outstanding.isEmpty()) {
            log.info("Sent {} payment reminder(s)", outstanding.size());
        }
    }
}
